package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ThreadsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.ASYNC_THREADS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("thr-mc-01", Topic.ASYNC_THREADS, 3,
                "Which is the preferred way to run code on a background thread in modern Java?",
                "Extend Thread and override run()",
                "Implement Runnable and pass to new Thread()",
                "Submit a Runnable or Callable to an ExecutorService",
                "Call Thread.start() directly",
                "c",
                "ExecutorService (from Executors.newFixedThreadPool() etc.) manages a thread pool, "
                + "reusing threads to avoid overhead. "
                + "Extending Thread or creating Thread per task is fine for simple use but doesn't scale. "
                + "CompletableFuture builds on this for async composition."),

            mc("thr-mc-02", Topic.ASYNC_THREADS, 3,
                "What does 'synchronized' do on a method?",
                "Makes the method run on a separate thread",
                "Ensures only one thread can execute the method on a given object at a time",
                "Makes the method faster by removing thread overhead",
                "Prevents the method from accessing shared state",
                "b",
                "synchronized acquires the object's intrinsic lock before executing the method body. "
                + "Other threads trying to call any synchronized method on the same object block until the lock is released. "
                + "This prevents data races on shared mutable state."),

            mc("thr-mc-03", Topic.ASYNC_THREADS, 3,
                "What is a data race?",
                "Two threads competing to start first",
                "Two or more threads accessing shared mutable data without synchronisation",
                "A thread that runs faster than expected",
                "A deadlock between exactly two threads",
                "b",
                "A data race occurs when multiple threads read/write shared state without synchronisation. "
                + "The result is undefined (the interleaving is non-deterministic). "
                + "Fix with synchronized, volatile, java.util.concurrent types, or immutable data."),

            mc("thr-mc-04", Topic.ASYNC_THREADS, 4,
                "What does CompletableFuture.supplyAsync(supplier) do?",
                "Runs the supplier synchronously and blocks until done",
                "Runs the supplier on the common ForkJoinPool and returns a Future for the result",
                "Schedules the supplier to run once per second",
                "Compiles the supplier to native code",
                "b",
                "supplyAsync runs the supplier asynchronously (typically on the common ForkJoinPool or a specified Executor) "
                + "and returns a CompletableFuture<T> you can chain with .thenApply(), .thenAccept(), etc., "
                + "without blocking the calling thread."),

            trace("thr-tr-01", Topic.ASYNC_THREADS, 3,
                "What is the range of possible outputs?",
                "class Counter {\n"
                + "    int count = 0;\n"
                + "}\n"
                + "Counter c = new Counter();\n"
                + "Thread t1 = new Thread(() -> { for (int i=0;i<1000;i++) c.count++; });\n"
                + "Thread t2 = new Thread(() -> { for (int i=0;i<1000;i++) c.count++; });\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(c.count);",
                "any value from 1000 to 2000",
                "count++ is not atomic — it reads, increments, then writes. "
                + "Two threads can read the same value simultaneously, losing an increment. "
                + "Result is anywhere from 1000 (maximum loss) to 2000 (no interleaving). "
                + "Fix: use AtomicInteger or synchronize."),

            debug("thr-db-01", Topic.ASYNC_THREADS, 4,
                "This code can deadlock. Why?",
                "Object lock1 = new Object(), lock2 = new Object();\n"
                + "Thread t1 = new Thread(() -> { synchronized(lock1) { synchronized(lock2) {} } });\n"
                + "Thread t2 = new Thread(() -> { synchronized(lock2) { synchronized(lock1) {} } });",
                "synchronized cannot be used inside lambdas",
                "Thread t1 and t2 may each acquire one lock and wait for the other, causing deadlock",
                "lock1 and lock2 must be the same object",
                "Thread.start() is missing",
                "b",
                "t1 acquires lock1 then waits for lock2. t2 acquires lock2 then waits for lock1. "
                + "Each holds what the other needs — circular wait = deadlock. "
                + "Fix: always acquire locks in the same order in all threads."),

            codegen("thr-cg-01", Topic.ASYNC_THREADS, 3,
                "Which creates a thread that prints 'Hello' and starts it correctly?",
                "Thread t = new Thread(); t.run(); // prints Hello",
                "Thread t = new Thread(() -> System.out.println(\"Hello\")); t.start();",
                "Thread.run(() -> System.out.println(\"Hello\"));",
                "new Runnable(() -> System.out.println(\"Hello\")).start();",
                "b",
                "Pass a Runnable lambda to the Thread constructor and call start() (not run()). "
                + "Calling run() directly executes on the current thread — no new thread is created. "
                + "Option C: Thread.run() is not a static method. "
                + "Option D: Runnable has no start() method."),

            mc("thr-mc-05", Topic.ASYNC_THREADS, 3,
                "What does Thread.join() do?",
                "Merges two threads into one",
                "Blocks the calling thread until the target thread finishes",
                "Pauses the current thread for a fixed time",
                "Interrupts the target thread",
                "b",
                "t.join() blocks the calling thread until thread t terminates. "
                + "Used to wait for background work to complete before proceeding. "
                + "Thread.sleep(ms) pauses for a fixed duration. "
                + "t.interrupt() sets the interrupted flag to signal a thread to stop."),

            trace("thr-tr-02", Topic.ASYNC_THREADS, 4,
                "What is the guaranteed output (ignoring thread scheduling)?",
                "AtomicInteger counter = new AtomicInteger(0);\n"
                + "Thread t1 = new Thread(() -> { for (int i=0;i<1000;i++) counter.incrementAndGet(); });\n"
                + "Thread t2 = new Thread(() -> { for (int i=0;i<1000;i++) counter.incrementAndGet(); });\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(counter.get());",
                "2000",
                "AtomicInteger.incrementAndGet() is atomic — thread-safe without synchronized. "
                + "Both threads each increment 1000 times. The result is always exactly 2000."),

            mc("thr-mc-06", Topic.ASYNC_THREADS, 3,
                "What does 'volatile' do on a field?",
                "Makes reads/writes atomic for all types",
                "Ensures all threads see the most up-to-date value by reading/writing directly to main memory",
                "Prevents the field from being garbage collected",
                "Makes the field thread-local (each thread has its own copy)",
                "b",
                "volatile guarantees visibility: a write by one thread is immediately visible to all others. "
                + "It does NOT guarantee atomicity for compound operations like i++ (read-modify-write). "
                + "For atomicity, use AtomicInteger or synchronized. "
                + "Use volatile for simple flags like 'boolean running = true;'."),

            codegen("thr-cg-02", Topic.ASYNC_THREADS, 3,
                "Which correctly runs a task on a thread pool and waits for completion?",
                "Thread t = new Thread(() -> task()); t.run(); t.join();",
                "ExecutorService pool = Executors.newFixedThreadPool(4); pool.submit(() -> task()); pool.shutdown(); pool.awaitTermination(1, TimeUnit.MINUTES);",
                "new Thread(() -> task()).start(); Thread.sleep(1000);",
                "CompletableFuture.runAsync(() -> task()).wait();",
                "b",
                "Submit to the pool, call shutdown() to stop accepting new tasks, then awaitTermination() "
                + "to block until all submitted tasks finish. "
                + "Option A calls run() (not start()) so no new thread is created. "
                + "Option C uses sleep() — a fixed wait that may be too short or too long. "
                + "Option D: wait() is an Object monitor method, not CompletableFuture.get()."),

            mc("thr-mc-07", Topic.ASYNC_THREADS, 3,
                "What is a deadlock?",
                "A thread that runs forever without stopping",
                "Two or more threads each holding a resource the other needs, blocking indefinitely",
                "A thread that uses too much memory",
                "A thread that throws an uncaught exception",
                "b",
                "Deadlock: Thread A holds lock1 and waits for lock2. Thread B holds lock2 and waits for lock1. "
                + "Neither can proceed. JVM doesn't automatically resolve deadlocks. "
                + "Prevention: always acquire locks in a consistent global order."),

            mc("thr-mc-08", Topic.ASYNC_THREADS, 3,
                "What is the difference between Runnable and Callable?",
                "Runnable can be scheduled; Callable cannot",
                "Callable can return a value and throw checked exceptions; Runnable cannot",
                "Runnable runs asynchronously; Callable runs synchronously",
                "Callable is used with threads; Runnable is used with lambdas",
                "b",
                "Runnable: void run() — no return, no checked exception. "
                + "Callable<V>: V call() throws Exception — returns a value and can throw. "
                + "Use Callable when you need to get a result back from a background thread via Future<V>."),

            mc("thr-mc-09", Topic.ASYNC_THREADS, 4,
                "What does AtomicInteger provide that int does not?",
                "Larger range of values",
                "Thread-safe atomic operations (compare-and-set, incrementAndGet) without locking",
                "Automatic garbage collection",
                "Support for negative values",
                "b",
                "int operations like i++ are NOT atomic — they involve read, modify, write. "
                + "AtomicInteger.incrementAndGet() is atomic by design (CAS — compare-and-swap). "
                + "No synchronized block needed for simple counters shared across threads."),

            trace("thr-tr-03", Topic.ASYNC_THREADS, 3,
                "What is printed?",
                "Thread t = new Thread(() -> System.out.println(\"background\"));\n"
                + "t.start();\n"
                + "t.join();\n"
                + "System.out.println(\"main\");",
                "background\nmain",
                "t.start() launches the background thread. t.join() blocks main until t finishes. "
                + "So 'background' prints before 'main'. Output order is guaranteed."),

            trace("thr-tr-04", Topic.ASYNC_THREADS, 4,
                "What does this code guarantee about output ordering?",
                "CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> \"Hello\")\n"
                + "    .thenApply(s -> s + \" World\");\n"
                + "System.out.println(cf.get());",
                "Hello World",
                "supplyAsync runs the supplier on a background thread returning \"Hello\". "
                + "thenApply appends \" World\". cf.get() blocks until the future completes and returns the result. "
                + "Output: \"Hello World\"."),

            debug("thr-db-02", Topic.ASYNC_THREADS, 3,
                "This code has a potential visibility problem. What is it?",
                "class Worker {\n"
                + "    boolean running = true;\n"
                + "    void stop() { running = false; }\n"
                + "    void run() { while (running) { /* work */ } }\n"
                + "}",
                "running must be static to be shared between threads",
                "Without volatile, the JVM may cache 'running' in a CPU register — the worker thread may never see the updated false value",
                "boolean fields cannot be assigned false",
                "stop() must be synchronized to change running",
                "b",
                "The JVM can cache field values in CPU registers for performance. "
                + "A thread calling stop() writes false, but the worker thread may still read the stale cached true. "
                + "Fix: declare 'volatile boolean running = true;' to force reads/writes through main memory."),

            debug("thr-db-03", Topic.ASYNC_THREADS, 4,
                "This code causes a race condition. Why?",
                "class BankAccount {\n"
                + "    private int balance = 1000;\n"
                + "    void withdraw(int amount) {\n"
                + "        if (balance >= amount) {\n"
                + "            balance -= amount;\n"
                + "        }\n"
                + "    }\n"
                + "}",
                "balance must be static",
                "The if-check and subtraction are two separate operations — another thread can withdraw between them",
                "int cannot hold negative values after subtraction",
                "balance must be volatile to be thread-safe",
                "b",
                "Thread A checks balance >= 500, then Thread B also passes the check (balance is still 1000). "
                + "Both subtract 500 — balance ends at 0 when only one withdrawal should succeed. "
                + "Fix: declare withdraw() synchronized, or use AtomicInteger with compareAndSet()."),

            codegen("thr-cg-03", Topic.ASYNC_THREADS, 3,
                "Which correctly creates a fixed thread pool, submits 10 tasks, and shuts down gracefully?",
                "ExecutorService ex = Executors.newFixedThreadPool(4); for (int i=0;i<10;i++) ex.execute(() -> doWork()); ex.shutdown();",
                "new Thread(() -> { for(int i=0;i<10;i++) doWork(); }).start();",
                "ExecutorService ex = Executors.newFixedThreadPool(10); ex.submit(this::doWork); ex.stop();",
                "ThreadPool.run(10, () -> doWork());",
                "a",
                "newFixedThreadPool(4) creates 4 threads. execute() submits each task. shutdown() stops accepting new tasks "
                + "after all 10 are submitted and waits for them to complete. "
                + "Option B runs all work on one thread serially. "
                + "Option C: stop() is not a valid ExecutorService method. "
                + "Option D: ThreadPool doesn't exist in standard Java.")
        );
    }
}
