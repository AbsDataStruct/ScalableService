package myService;

import java.util.concurrent.atomic.AtomicLong;

import others.DataBaseConnection;
import others.PreparedStatement;

class MyServiceInstance implements Runnable {

	private static final String DATABASE_ACCESS = "oracle://mydb"; // database connection parameters
	private static final String STATEMENT_QUERY = "SELECT"; // database query performed
	private static AtomicLong instanceLastId = new AtomicLong(); // Id attribution

	private final long instanceId;
	private final Thread instanceThread;

	MyServiceInstance() throws Exception {
		this.instanceId = instanceLastId.getAndIncrement();
		this.instanceThread = new Thread(this);
		this.instanceThread.start();
		System.out.format("%d Instance %d: Instance Creation\n", System.currentTimeMillis(), instanceId);
	}

	// Running thread for the worker instance
	@Override
	public final void run() {
		System.out.format("%d Instance %d: Instance started\n", System.currentTimeMillis(), instanceId);

		DataBaseConnection connection = new DataBaseConnection();
		PreparedStatement statement = null;
		try {
			connection.connectDatabase(DATABASE_ACCESS);
			statement = connection.prepareStatement(STATEMENT_QUERY);

			do {
				try {
					// Get a new task to run
					MyServiceTask taskToRun = MyService.consume();
					if (taskToRun != null) {
						System.out.format("%d Instance %d: Got a task from %s\n", System.currentTimeMillis(),
								instanceId, taskToRun.parameters.get(0));

						// Query the database
						statement.setParameter(taskToRun.parameters);
						statement.executeQuery();

						// Run the task
						taskToRun.run();
						System.out.format("%d Instance %d: Task from %s completed\n", System.currentTimeMillis(),
								instanceId, taskToRun.parameters.get(0));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (MyService.shouldMaintainInstance(this));

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (statement != null)
				statement.close();
			// Ensure close of database connection
			try {
				connection.closeDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.format("%d Instance %d: Instance terminated\n", System.currentTimeMillis(), instanceId);
	}

}