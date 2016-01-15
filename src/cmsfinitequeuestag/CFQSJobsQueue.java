package cmsfinitequeuestag;

import controlmidleserverstag.CMSSJobsQueue;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dangbk on 17/04/2015.
 */
public class CFQSJobsQueue<E> extends CMSSJobsQueue<E> {
    protected int numberJobLost = 0;
    protected int capacity = 1000;
    protected int threshold =  800;

    public CFQSJobsQueue(){
        super();
        this.capacity = CFQSHelper.jobsqueuecapacity;
        this.threshold = CFQSHelper.jobsqueuethresholdup;
    }

    protected boolean startCout = false;

    public boolean add(E e){
        // phuogn thuc nay tra lai false neu hang doi day (vuot qua threshold)
        if(jobsqueue.size() >= capacity){
            if (startCout)
            numberJobLost ++;
            else {
                if(CloudSim.clock() > CFQSHelper.timeStartSimulate) startCout = true;
            }
            return false;
        }
        // khong them job duoc nua vi da vuot qua gioi han
        else
        return jobsqueue.add(e);
    }


    public int getNumberJobLost(){
        return numberJobLost;
    }


}
