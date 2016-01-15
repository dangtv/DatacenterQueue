package controlmidleservernostag;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Created by dangbk on 20/04/2015.
 */
public class CMSNSJob {
    public static int jobcount = 0;
    public int jobid = 0;
    public double waittime = 0;
    private double timeCreate = 0;
    private double timeStartExe = 0;
    private double timeComplete = 0;
    CMSNSHost host = null;
    public CMSNSJob(){
        timeCreate = CloudSim.clock();
        jobid = this.jobcount;
        jobcount ++;
    }

    public double getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(double timeCreate) {
        this.timeCreate = timeCreate;
    }

    public double getTimeStartExe() {
        return timeStartExe;
    }

    public void setTimeStartExe(double timeStartExe) {
        this.timeStartExe = timeStartExe;
    }

    public double getTimeComplete() {
        return timeComplete;
    }

    public void setTimeComplete(double timeComplete) {
        this.timeComplete = timeComplete;
    }

    public CMSNSHost getHost() {
        return host;
    }

    public void setHost(CMSNSHost host) {
        this.host = host;
    }
}
