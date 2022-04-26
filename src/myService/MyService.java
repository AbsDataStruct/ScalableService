package myService;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import others.ResultSet;

public final class MyService {
	public static final long REMOVING_INSTANCE_UNDER_REQUEST_TIME_MILLIS = 2000;
	public static final long ADDING_INSTANCE_ABOVE_REQUEST_TIME_MILLIS = 8000;
	public static final int NUMBER_OF_RECORDED_LAST_REQUESTS_TIMES = 3;

	// synchronization for Task queuing
	private static ReentrantLock taskQueuingLock = new ReentrantLock();
	private static Condition waitForTaskEnd = taskQueuingLock.newCondition();
	private static final Queue<MyServiceTask> tasks = new LinkedBlockingQueue<>();

	// synchronization for Instances scalability
	private static ReentrantLock instanceScalabilityLock = new ReentrantLock();
	private static final Set<MyServiceInstance> myServiceInstances = new HashSet<>();
	private static int instancesCount = 0;
	private static int shouldReduceInstance = 0;
	private static long[] lastRequestsTimes = new long[NUMBER_OF_RECORDED_LAST_REQUESTS_TIMES];
	private static int requestsTimesPointer = 0;
	private static long lastRequestsTimesTotal = 0;

	public static final void init() {
		// Create the first instance if not yet created
		instanceScalabilityLock.lock();
		try {
			if (instancesCount == 0) {
				System.out.format("%d: First instance to be created\n", System.currentTimeMillis());
				createNewInstance();
			}
		} finally {
			instanceScalabilityLock.unlock();
		}
	}

	// Function called by producer thread (client)
	public static final ResultSet produce(List<String> parameters) throws InterruptedException {
		long requestTimeMillis = System.currentTimeMillis();
		System.out.format("%d %s: Request\n", System.currentTimeMillis(), parameters.get(0));
		MyServiceTask newTask;
		taskQueuingLock.lock();
		try {
			// Create a new task
			System.out.format("%d %s: Produce a task\n", System.currentTimeMillis(), parameters.get(0));
			newTask = new MyServiceTask(parameters);

			// Insert the task in the queue
			tasks.add(newTask);
			System.out.format("%d %s: (+) Adding a task, %d tasks in the queue with %d instances\n",
					System.currentTimeMillis(), parameters.get(0), tasks.size(), instancesCount);

			// Notifies the consumer threads that now it can start consuming
			waitForTaskEnd.signalAll();
		} finally {
			taskQueuingLock.unlock();
		}

		// Wait for the result being available
		System.out.format("%d %s: Waiting result\n", System.currentTimeMillis(), parameters.get(0));
		taskQueuingLock.lock();
		try {
			while (!newTask.isDone())
				waitForTaskEnd.await();
		} finally {
			taskQueuingLock.unlock();
		}

		// Calculate request time
		instanceScalabilityLock.lock();
		try {
			long requestTotalTimeMillis = System.currentTimeMillis() - requestTimeMillis;
			lastRequestsTimesTotal += requestTotalTimeMillis - lastRequestsTimes[requestsTimesPointer];
			lastRequestsTimes[requestsTimesPointer] = requestTotalTimeMillis;
			requestsTimesPointer = Math.floorMod(requestsTimesPointer + 1, NUMBER_OF_RECORDED_LAST_REQUESTS_TIMES);

			// Adjusting number of instances
			String adjustmentDecision = "... no scale change";
			if (lastRequestsTimesTotal > ADDING_INSTANCE_ABOVE_REQUEST_TIME_MILLIS
					* NUMBER_OF_RECORDED_LAST_REQUESTS_TIMES) {
				adjustmentDecision = "asked to ADD instance";
				createNewInstance();
			} else if (lastRequestsTimesTotal < REMOVING_INSTANCE_UNDER_REQUEST_TIME_MILLIS
					* NUMBER_OF_RECORDED_LAST_REQUESTS_TIMES) {
				adjustmentDecision = "asking to TERMINATE instance";
				shouldReduceInstance++;
			}

			// Retrieve the result
			System.out.format("%d %s: ----- Return result in %dms ----- with %d instances\n",
					System.currentTimeMillis(), parameters.get(0), requestTotalTimeMillis, instancesCount);
			System.out.format("%d %s: |||| AVERAGE TIME %dms ||||, %s\n", System.currentTimeMillis(), parameters.get(0),
					Math.floorDiv(lastRequestsTimesTotal, NUMBER_OF_RECORDED_LAST_REQUESTS_TIMES), adjustmentDecision);
		} finally {
			instanceScalabilityLock.unlock();
		}
		return newTask.getResult();
	}

	// Function called by consumer thread
	static final MyServiceTask consume() throws InterruptedException {
		taskQueuingLock.lock();
		try {
			MyServiceTask nextTask = null;
			while (nextTask == null) {
				nextTask = tasks.poll();
				if (nextTask == null)
					waitForTaskEnd.await();
			}
			return nextTask;
		} finally {
			taskQueuingLock.unlock();
		}
	}

	// Decide to maintain an instance thread
	static final boolean shouldMaintainInstance(MyServiceInstance aServiceInstance) {
		instanceScalabilityLock.lock();
		try {
			if (shouldReduceInstance > 0) {
				shouldReduceInstance--;
				if (instancesCount > 1) {
					assert myServiceInstances.contains(aServiceInstance);
					myServiceInstances.remove(aServiceInstance);
					instancesCount--;
					System.out.format("%d: *** Instance terminated, currently %d instances\n",
							System.currentTimeMillis(), instancesCount);
					return false;
				}
			}
		} finally {
			instanceScalabilityLock.unlock();
		}
		return true;
	}

	// Add a worker instance thread
	private static final void createNewInstance() {
		System.out.format("%d: Adding new worker instance\n", System.currentTimeMillis());
		instanceScalabilityLock.lock();
		try {
			MyServiceInstance newInstance = new MyServiceInstance();
//			Thread newInstanceThread = new Thread(newInstance);
			// newInstanceThread.start();
			myServiceInstances.add(newInstance);
			instancesCount++;
			shouldReduceInstance = 0;
			System.out.format("%d: *** Instance added, currently %d instances\n", System.currentTimeMillis(),
					instancesCount);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Could not create new instance");
		} finally {
			instanceScalabilityLock.unlock();
		}
	}

	static final void newResultFound() {
		taskQueuingLock.lock();
		try {
			waitForTaskEnd.signalAll();
		} finally {
			taskQueuingLock.unlock();
		}
	}
}
