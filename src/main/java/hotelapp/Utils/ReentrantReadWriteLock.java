package hotelapp.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom reentrant read/write lock that allows:
 * 1) Multiple readers (when there is no writer). Any thread can acquire multiple read locks (if nobody is writing).
 * 2) One writer (when nobody else is writing or reading).
 * 3) A writer is allowed to acquire a read lock while holding the write lock.
 * 4) A writer is allowed to acquire another write lock while holding the write lock.
 * 5) A reader can not acquire a write lock while holding a read lock.
 *
 * Use ReentrantReadWriteLockTest to test this class.
 * The code is modified from the code of Prof. Rollins.
 */
public class ReentrantReadWriteLock {

    // current writers
    private int writers;
    // read Map Key - Reader Thread ID, Value - number of thread instances having the read lock
    private Map<Long, Integer> readMap;
    // current writer thread ID
    private long currentWriter;


    /**
     * Constructor for ReentrantReadWriteLock
     */
    public ReentrantReadWriteLock() {
        this.writers = 0;
        this.readMap = new HashMap<>();
        this.currentWriter = -1;
        // FILL IN CODE: initialize instance variables

    }

    /**
     * Return true if the current thread holds a read lock.
     *
     * @return true or false
     */
    public synchronized boolean isReadLockHeldByCurrentThread() {
        long currentThreadId = Thread.currentThread().getId();
        return readMap.containsKey(currentThreadId);
    }

    /**
     * Return true if the current thread holds a write lock.
     *
     * @return true or false
     */
    public synchronized boolean isWriteLockHeldByCurrentThread() {
        long currentThreadId = Thread.currentThread().getId();
        return currentThreadId == currentWriter;
    }

    /**
     * Non-blocking method that attempts to acquire the read lock. Returns true
     * if successful.
     * Checks conditions (whether it can acquire the read lock), and if they are true,
     * updates readers info.
     *
     * Note that if conditions are false (can not acquire the read lock at the moment), this method
     * does NOT wait, just returns false
     * @return
     */
    public synchronized boolean tryAcquiringReadLock() {
        if(currentWriter>0) {
            if(currentWriter == Thread.currentThread().getId()) {
                addToReadMap(Thread.currentThread().getId());
                return true;
            }
            return false;
        }
        addToReadMap(Thread.currentThread().getId());
        return true;
    }

    private void addToReadMap(long id) {
        if(readMap.containsKey(id)) {
            readMap.put(id, readMap.get(id)+1);
        }
        else {
            readMap.put(id, 1);
        }
    }

    /**
     * Non-blocking method that attempts to acquire the write lock. Returns true
     * if successful.
     * Checks conditions (whether it can acquire the write lock), and if they are true,
     * updates writers info.
     *
     * Note that if conditions are false (can not acquire the write lock at the moment), this method
     * does NOT wait, just returns false
     *
     * @return
     */
    public synchronized boolean tryAcquiringWriteLock() {
       if(readMap.size() == 0) {
           if(writers > 0) {
               if(currentWriter == Thread.currentThread().getId()) {
                   writers++;
                   return true;
               }
               else {
                   return false;
               }
           }
           else {
               currentWriter = Thread.currentThread().getId();
               writers++;
               return true;
           }
       }
       return false;
    }

    /**
     * Blocking method that will return only when the read lock has been
     * acquired.
     * Calls tryAcquiringReadLock, and as long as it returns false, waits.
     * Catches InterruptedException.
     *
     */
    public synchronized void lockRead() {
        if(!tryAcquiringReadLock()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Releases the read lock held by the calling thread. Other threads might
     * still be holding read locks. If no more readers after unlocking, calls notifyAll().
     */
    public synchronized void unlockRead() {
        long currentThreadId = Thread.currentThread().getId();
        if(readMap.containsKey(currentThreadId)) {
            if(readMap.get(currentThreadId) == 1) {
                readMap.remove(currentThreadId);
                notifyAll();
            }
            else
                readMap.put(currentThreadId, readMap.get(currentThreadId)-1);
        }
    }

    /**
     * Blocking method that will return only when the write lock has been
     * acquired.
     * Calls tryAcquiringWriteLock, and as long as it returns false, waits.
     * Catches InterruptedException.
     */
    public synchronized void lockWrite() {
        if (!tryAcquiringWriteLock()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Releases the write lock held by the calling thread. The calling thread
     * may continue to hold a read lock.
     * If the number of writers becomes 0, calls notifyAll.
     */

    public synchronized void unlockWrite() {
        if(writers > 0 && currentWriter == Thread.currentThread().getId()) {
            writers--;
            if(writers == 0) {
                currentWriter = -1;
                notifyAll();
            }
        }
    }
}
