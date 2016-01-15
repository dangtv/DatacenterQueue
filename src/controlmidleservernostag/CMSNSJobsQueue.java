package controlmidleservernostag;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dangbk on 17/04/2015.
 */
public class CMSNSJobsQueue<E>  {
    protected double waitingTime = 0;
    protected double lastUpdate = 0;
    protected Queue<E> jobsqueue = new LinkedList<>();

    protected long dem = 0;
    protected double timeOfQueue = 0;

    protected double oldMeanWaitingTime = 0;
    protected double oldMeanQueueLength = 0;


    public CMSNSJobsQueue(){
    }


    public boolean add(E e){

        ((CMSNSJob) e).setTimeCreate(CloudSim.clock());
        return jobsqueue.add(e);
    }
    public E poll() {

        return jobsqueue.poll();
    }





    public int getsize(){
        return jobsqueue.size();
    }

    public boolean isEmpty() {
        return jobsqueue.isEmpty();
    }
    public double getMeanWaitingTime(){
//        return tong/(solan -2);

        return waitingTime / dem;
    }
    public double getMeanQueueLength() {
        return oldMeanQueueLength;
    }
    public long getTotalItem(){
        return dem;
    }
}
