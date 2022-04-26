package myService;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import others.ResultSet;

class MyServiceTask implements Runnable {

	private final Random random = new Random();
	final List<String> parameters; // not private here to get the client number for debug from the executor
									// MyServiceInstance

	private AtomicBoolean isDone = new AtomicBoolean();
	private ResultSet result;

	MyServiceTask(List<String> parameters) {
		System.out.format("%d %s: Create a task\n", System.currentTimeMillis(), parameters.get(0));
		// startTimeMillis = System.currentTimeMillis();
		this.parameters = parameters;
		this.isDone.set(false);
	}

	final boolean isDone() {
		return isDone.get();
	}

	final ResultSet getResult() {
		if (!isDone.get())
			throw new Error("");
		System.out.format("%d %s: Retrieve the Result\n", System.currentTimeMillis(), parameters.get(0));
		ResultSet r = result;
		result = null;
		return r;
	}

	// Task to be performed on request call
	@Override
	public final void run() {
		// process query
		System.out.format("%d Instance x %s: Process the query\n", System.currentTimeMillis(), parameters.get(0));
		try {
			long processTime = Math.round(Math.max(0, random.nextGaussian() * 2000 + 3000));
			System.out.format("%d Instance x %s: Sleep %dms\n", System.currentTimeMillis(), parameters.get(0),
					processTime);
			Thread.sleep(processTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result = new ResultSet(); // Here we put the result of the request
		System.out.format("%d Instance x %s: Notify\n", System.currentTimeMillis(), parameters.get(0));
		isDone.set(true);
		MyService.newResultFound();
		System.out.format("%d Instance x %s: Done\n", System.currentTimeMillis(), parameters.get(0));
	}

	@Override
	public void finalize() {
		if (this.result != null)
			this.result.close();
	}
}
