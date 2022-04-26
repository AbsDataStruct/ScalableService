package clients;

import java.util.ArrayList;
import java.util.List;

import myService.MyService;
import others.ResultSet;

public final class Client implements Runnable {
	private final String callerName;

	Client(String clientName) {
		callerName = clientName;
	}

	// Running thread for the client instance
	@Override
	public final void run() {
		while (true) {

			// wait some random amount of time to simulate client's needs. It was set by
			// setExpectedSleepMillis()
			try {
				Thread.sleep(StartEmulation.clientSleepTimeMillis);
				System.out.format("%d %s: Client sleep for %dms\n", System.currentTimeMillis(), callerName,
						StartEmulation.clientSleepTimeMillis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// call the service
			long startMillis = System.currentTimeMillis();
			System.out.format("%d %s: Service call\n", System.currentTimeMillis(), callerName);
			ResultSet result = null;
			try {
				List<String> parameters = new ArrayList<>();
				parameters.add(callerName); // give the "client xxx" number as parameter for debugging purpose
				result = MyService.produce(parameters);

				// -- Process the result here --

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (result != null)
					result.close();
			}
			System.out.format("%d %s: Service call returned after %d millis\n", System.currentTimeMillis(), callerName,
					System.currentTimeMillis() - startMillis);
		}
	}
}