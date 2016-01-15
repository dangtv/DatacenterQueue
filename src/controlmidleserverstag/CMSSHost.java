package controlmidleserverstag;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

/**
 * Created by dang on 21/04/2015.
 */
public class CMSSHost extends Host {

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param vmScheduler    the currentvm scheduler
     */
    protected CMSSJob job = null;
    public CMSSHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
    }

    public CMSSJob getJob() {
        return job;
    }

    public void setJob(CMSSJob job) {
        if(job != null) {
            job.setTimeStartExe(CloudSim.clock());
            this.job = job;
        }
//        if(currentvm == null) {
//            Log.printLine("loi host chua tao Vm");
//            return;
//        }




    }
    protected Vm currentvm;

    public void setCurrentvm(Vm currentvm) {
        this.currentvm = currentvm;
    }

    public Vm getCurrentvm() {
        return currentvm;
    }
}
