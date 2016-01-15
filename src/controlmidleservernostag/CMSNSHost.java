package controlmidleservernostag;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

/**
 * Created by dang on 21/04/2015.
 */
public class CMSNSHost  {

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param vmScheduler    the vm scheduler
     */
    private CMSNSJob job = null;
    public static int hostcount = 0;
    public int hostid;
    public CMSNSHost(){
        hostid = hostcount;
        hostcount++;
    }

    public double timeStartSetup;
    public double timeSetupComplete;
    public double timeSetupExpect;
    public boolean isSetUpmode = false;
    public CMSNSJob getJob() {
        return job;
    }

    public void setJob(CMSNSJob job) {
        if(job != null)
            job.setTimeStartExe(CloudSim.clock());
        this.job = job;
    }
}
