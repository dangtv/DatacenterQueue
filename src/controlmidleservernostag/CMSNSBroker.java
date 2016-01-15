package controlmidleservernostag;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CMSNSBroker extends DatacenterBroker {

    private double lamda;


    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *             simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public CMSNSBroker(String name, double _lamda) throws Exception {

        super(name);
        this.lamda = _lamda;

    }
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
//            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
//                    + " has been created in Datacenter #" + mainDatacenterId + ", Host #"
//                    + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
                    + " failed in Datacenter #" + datacenterId);
        }

        incrementVmsAcks();

    }

    protected void createVmsInDatacenter(int datacenterId){
        submitCloudlets();
    }

    protected void submitCloudlets() {
//        Log.printLine("broker send the cloudlet first");
        sendCloudlet();
        sendNow(CMSNSHelper.getDatacenterId(), CMSNSConstants.ControlMiddleHostEvent);
    }

    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // if the simulation finishes
            case CMSNSConstants.sendCloudletEvent:
                sendCloudlet();
                break;
            // other unknown tags are processed by this method
            default:

                break;
        }
        if (ev == null) {
            Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null. dd" + ev.getTag());
            return;
        }



    }

    private void sendCloudlet() {


//        if(CMSHelper.getCloudletid() > CMSHelper.totalJobs) CloudSim.terminateSimulation();
            CMSNSJob cloudlet = CMSNSHelper.createJob(getId());
//            Log.printLine(CloudSim.clock() + " : broker send  cloudlet " + cloudlet.getCloudletId());
            sendNow(CMSNSHelper.getDatacenterId(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            send(getId(), CMSNSHelper.arrivaltime.pop().doubleValue(), CMSNSConstants.sendCloudletEvent);
//        }
    }


    protected void processCloudletReturn(SimEvent ev) {
        CMSNSJob cloudlet = (CMSNSJob) ev.getData();
//        listJob.add(cloudlet);

//        Log.printLine("da nhan job");
        if(start) {

            numberofjob++;
            if (CloudSim.clock() > CMSNSHelper.totalTimeSimulate) {
                Log.printLine(CloudSim.clock());
                CloudSim.terminateSimulation();
            }
//        if(numberofjob > jobbatdau )
            totalwaitingTime = totalwaitingTime - cloudlet.getTimeCreate() + cloudlet.getTimeStartExe();
        }
        else {
            if(CloudSim.clock() > CMSNSHelper.timeStartSimulate) start = true;
        }

    }

    public double totalwaitingTime = 0;

    public int numberofjob = 0;
//    public int jobbatdau = 00000000;
//    public int jobketthuc = 50000000;



    public boolean start = false;

    protected void clearDatacenters() {

    }
    public List<CMSNSJob> listJob = new ArrayList<>();
    public double getMeanWaittingTime(){
        return totalwaitingTime/(numberofjob );
    }
}
