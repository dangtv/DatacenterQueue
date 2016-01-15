package controlmidleserverstag;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dangbk on 20/04/2015.
 */
public class CMSSMiddleHostQueue<E>  {
    protected double waitingTime = 0;
    protected double lastUpdate = 0;
    protected Queue<E> middleQueue = new LinkedList<>();
    protected double timeEmpty = 0;

    protected long dem = 0;
    protected double timeOfQueue = 0;

    protected double oldMeanWaitingTime = 0;
    protected double oldMeanQueueLength = 0;

    public double getTimeEmpty() {
        return timeEmpty;
    }
    public CMSSMiddleHostQueue(){
    }
    public boolean add(E e){

        updateWaitingtime();
        return middleQueue.add(e);
    }
    public E poll() {
        updateWaitingtime();
        return middleQueue.poll();
    }
    private double time = 0;
    public void updateWaitingtime(){
        time = CloudSim.clock();
        if(start) {
            waitingTime = waitingTime + (time - lastUpdate) * ( middleQueue.size());
            timeOfQueue = timeOfQueue + (time - lastUpdate);

        }
        else {
            if(time > CMSSHelper.timeStartSimulate) start = true;
        }
        lastUpdate = time;
    }


    private boolean start = false;

    public int getsize(){
        return middleQueue.size();
    }

    public boolean isEmpty() {
        return middleQueue.isEmpty();
    }

    public double getMeanQueueLength() {
        return waitingTime / timeOfQueue;
    }
    public double getTotalWaitingTime(){
        return waitingTime;
    }
    public double getTimeOfQueue(){
        return timeOfQueue;
    }
}
