package mono.backend.process;

import java.util.LinkedList;
import java.util.Queue;

public class MsgQueue {
	private Queue<Object> queue = new LinkedList<Object>();

    public Queue<Object> getQueue() {
        return queue;
    }

    // Inserts the specified element into this queue if it is possible to do so
    // immediately without violating capacity restrictions
    public void add(Object value) {
        synchronized (queue) {
            queue.add(value);
        }
    }

    // Removes a single instance of the specified element from this collection
    public void remove(Object value) {
        synchronized (queue) {
            queue.remove(value);
        }
    }
    

	public void removeAll() {
        synchronized (queue) {
            queue.clear();
        }
    }

    // Retrieves and removes the head of this queue, or returns null if this
    // queue is empty.
    public Object poll() {
    	synchronized (queue) {
	        Object data = queue.poll();
	        return data;
    	}
    }

    // Returns true if this collection contains no elements
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // Returns the number of elements in this collection. If this collection
    // contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE
    public int getSize() {
        return queue.size();
    }
}
