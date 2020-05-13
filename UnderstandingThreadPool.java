
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


interface ThreadPool<Job extends Runnable>
{
    void execute(Job job);
    void shutdown();
    void addWorker(int num);
    void removeWorker(int num);
    int getJobSize();
}

public class UnderstandingThreadPool<Job extends Runnable> implements ThreadPool<Job>
{
    private static final int MAX_WORKER_NUMBERS = 10;
    private static final int DEFAULT_WORKER_NUMBERS = 5;
    private static final int MIN_WORKER_NUMBERS = 1;
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    private final List<Worker> workers = java.util.Collections.synchronizedList(new ArrayList<Worker>());
    private int workerNum = DEFAULT_WORKER_NUMBERS;
    private AtomicLong threadNum = new AtomicLong();
    public UnderstandingThreadPool()
    {
        initializeWokers(DEFAULT_WORKER_NUMBERS);
    }
    public UnderstandingThreadPool(int num)
    {
        workerNum = num > MAX_WORKER_NUMBERS ? MAX_WORKER_NUMBERS : num < MIN_WORKER_NUMBERS ? MIN_WORKER_NUMBERS : num;
        initializeWokers(workerNum);
    }
    private void initializeWokers(int num)
    {
        for (int i=0; i<num; i++)
        {
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker, "AboutDemoThreadPool-Worker-"+threadNum.incrementAndGet());
            thread.start();
        }
    }

    @Override
    public void execute(Job job) {
        if (job != null)
        {
            synchronized (jobs)
            {
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }

    @Override
    public void shutdown() {
        for (Worker worker : workers)
        {
            worker.shutdown();
        }
    }

    @Override
    public void addWorker(int num) {
        synchronized (jobs)
        {
            if (num+this.workerNum>MAX_WORKER_NUMBERS)
            {
                num = MAX_WORKER_NUMBERS - this.workerNum;
            }
            initializeWokers(num);
            this.workerNum+=num;
        }
    }

    @Override
    public void removeWorker(int num) {
        synchronized (jobs)
        {
            if (num>=this.workerNum)
            {
                throw new IllegalArgumentException("beyond worknum!");
            }
            int count=0;
            while (count<num)
            {
                Worker worker = workers.get(count);
                if (workers.remove(worker))
                {
                    worker.shutdown();
                    count++;
                }
                this.workerNum-=count;
            }
        }
    }

    @Override
    public int getJobSize() {
        return jobs.size();
    }
    
    class Worker implements Runnable
    {
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running)
            {
                Job job = null;
                synchronized (jobs)
                {
                    while (jobs.isEmpty())
                    {
                        try
                        {
                            jobs.wait();
                        }
                        catch (InterruptedException ex)
                        {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    job = jobs.removeFirst();
                }
                if (job!=null)
                {
                    try
                    {
                        job.run();
                    }
                    catch (Exception ex)
                    {
                        ;
                    }
                }
            }
        }
        
        public void shutdown()
        {
            running = false;
        }
    }
    
    public static void main(String[] args)
    {
        class Cc implements Runnable
        {

            @Override
            public void run() {
                System.out.println("Cc");
            }
            
        }
        new UnderstandingThreadPool<Cc>(1).execute(new Cc());
    }
}
