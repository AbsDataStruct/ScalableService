package clients;

import myService.MyService;

public final class StartEmulation {
	public static final int CLIENT_COUNT = 20;
	public static final long LOAD_CHANGING_INTERVAL_MILLIS = 60000;
	public static final long DATABASE_QUERY_TIME_MILLIS = 500;
	public static final long MAX_RANDOM_CLIENT_SLEEP_TIME_MILLIS = 3000;

	public static volatile long clientSleepTimeMillis = 1000;

	public static final void main(String[] args) {
		System.out.println("START");
		MyService.init();
		System.out.format("Load changed with sleeping time of %d millis\n",clientSleepTimeMillis);

		// Creating Clients
		for (int client=0 ; client<CLIENT_COUNT ; client++) {
			new Thread(new Client("Client "+client)).start();
			System.out.format("CLIENT #%d CREATED\n",client);
		}

		// Automatic system Load tuning
		try {
			while (true) {
				clientSleepTimeMillis = Math.round(Math.random()*MAX_RANDOM_CLIENT_SLEEP_TIME_MILLIS);
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("Load changed with sleeping time of %d millis\n",clientSleepTimeMillis);
				System.out.format("Load changed with sleeping time of %d millis\n",clientSleepTimeMillis);
				System.out.format("Load changed with sleeping time of %d millis\n",clientSleepTimeMillis);
				System.out.format("Load changed with sleeping time of %d millis\n",clientSleepTimeMillis);
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("**********************************************************************************************************************************************\n");
				System.out.format("MEMORY USAGE %d Mb\n",Math.floorDiv(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory(),1024*1024));
				System.gc(); // Force Garbage collection
				Thread.sleep(LOAD_CHANGING_INTERVAL_MILLIS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("END");
	}

}
