package controlmidleserverstag;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by dangbk on 15/04/2015.
 */
public class CMSSDatacenter extends Datacenter {

    public CMSSJobsQueue<CMSSJob> jobsqueue = new CMSSJobsQueue<>();
    public Queue<Host> listHostOff = new LinkedList<>();
    public CMSSMiddleHostQueue<Host> listHostMIDDLE = new CMSSMiddleHostQueue<>();

    // them 2 bien nay de tinh luong may trung binh (gia hang doi)
    public CMSSMiddleHostQueue<Object> hostOnNumber = new CMSSMiddleHostQueue<>();
    public CMSSMiddleHostQueue<Object> hostSetupNumber = new CMSSMiddleHostQueue<>();
    //-----------------

    public List<Host> listHostON;
    public Host hostInSetupMode = null;
    //    private Host hostInOffToOn = null;
    public double alpha;
    public double controlTime;
    public double muy;
    public double lamda;
    public double timeOffToMiddle;
    public double verifySetupMode = -1;
    public boolean hasMiddle = true;

    public void setHasMiddle(boolean hasMiddle) {
        this.hasMiddle = hasMiddle;
    }


    public CMSSDatacenter(String name, DatacenterCharacteristics characteristics,
                          VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                          double schedulingInterval, double _alpha,
                          double _muy, double _lamda, double controltime,
                          double _timeOffToMiddle) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.alpha = _alpha;
        this.muy = _muy;
        this.lamda = _lamda;
        this.controlTime = controltime;
        this.timeOffToMiddle = _timeOffToMiddle;

        listHostOff.addAll(this.getHostList());
        listHostON = vmAllocationPolicy.getHostList();


    }

    public void setStartState(int numberOnServer, int numberMiddleServer) {
        for (int i = 0; i < numberOnServer; i++) {
            listHostON.add(listHostOff.poll());
            hostOnNumber.add(new Object());
        }
        for (int i = 0; i < numberMiddleServer; i++) {
            listHostMIDDLE.add(listHostOff.poll());
        }
    }

    // xu ly su kien moi
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics inquiry
            case CMSSConstants.TurnONSuccess:
                processTurnONSuccess(ev);
                break;

            // Resource dynamic info inquiry
            case CMSSConstants.MiddleSucess:
                processMiddleSuccess(ev);
                break;
            case CMSSConstants.ControlMiddleHostEvent:
                controlMiddleHost();
                break;
            case CMSSConstants.JobComplete:
                processJobComplete(ev);
                break;
            default:
                if (ev == null) {
                    Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
                }
                break;
        }
    }

    protected void processJobComplete(SimEvent ev) {
        CMSSJob job =(CMSSJob) ev.getData();
        ((CMSSHost)job.getHost()).setJob(null);
//        CMSJob job =(CMSJob) ev.getData();
        job.setTimeComplete(CloudSim.clock()); // lenh nay khong can thiet

//                        Log.printLine("so host on: " + listHostON.size() + " so host off:" + listHostOff.size() + " so host middle: " + listHostMIDDLE.getsize());

//        Log.printLine(CloudSim.clock() + " : cloudlet " + job.getCloudletId() + " complete");

        sendNow(CMSSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, job);
        // neu cloudlet duoc da hoan thanh
        // kiem tra hang doi va tat host:
        if (!jobsqueue.isEmpty()) { // neu co
            // chuyen ngay cloudlet do cho host vua thuc hien xong cloudlet do
//            Log.printLine("host "+ job.getHost().getId()+" da thuc hien xong job "+
//                    job.getCloudletId());
            assignJobToHost(jobsqueue.poll(), job.getHost());
            // kiem tra tiep neu hang doi khong con thi tat may dang trong setup mode
            if (jobsqueue.isEmpty()) turnOffHostInSetupMode();
        } else { // neu hang doi khong con
            // tat host vua thuc hien xong di
            turnOffHostOn(job.getHost());

            // tat host o trang thai setup di
            turnOffHostInSetupMode();
        }


    }


    protected void processMiddleSuccess(SimEvent ev) {

        Host h = (Host) ev.getData();
//        Log.printLine(CloudSim.clock()+ " : host "+h.getId()+" turn to Middle success");
        listHostMIDDLE.add(h);
        if (!jobsqueue.isEmpty()) {

            checkAndTurnMiddleToOn();
        }
    }

    public void controlMiddleHost() {
        if (hasMiddle) {
//            listHostMIDDLE.updateWaitingtime();
//            if ((CMSHelper.getCloudletid() < CMSHelper.getNumberToUpdateQueue()) || !jobsqueue.isEmpty()) {

//        if((CloudSim.clock() < CMSHelper.getNumberToUpdateQueue()) ||  !jobsqueue.isEmpty() ) {
            // check dieu kien bat may neu thoa man thi bat may tu off sang Middle
//                if ((listHostON.size() * muy < lamda) && (listHostMIDDLE.getsize() < 100)) {

            // tinh so may can bat:
            double timenext = 1 / (CMSSHelper.lamda * CMSSHelper.alpha / (CMSSHelper.lamda + CMSSHelper.alpha));

//            Log.printLine(CMSHelper.controlNum );
//            for(int i =0; i< CMSHelper.controlNum; i++)
            if (!listHostOff.isEmpty()) {

                turnAOffToMiddle();
            }
//                }
//            else {
            //tat may middle
//                    if(! listHostMIDDLE.isEmpty()) listHostOff.add(listHostMIDDLE.poll());
//                }
            send(getId(), timenext, CMSSConstants.ControlMiddleHostEvent);

//            }

        }
    }

    protected void turnAOffToMiddle() {
        if (!listHostOff.isEmpty()) {
//            Log.printLine(CloudSim.clock()+" : turn a off server to middle");
            send(getId(), CMSSHelper.timeOffToMiddle, CMSSConstants.MiddleSucess, listHostOff.poll());
        }
    }

    // override xu ly su kien cloudlet moi den:
    public void processCloudletSubmit(SimEvent ev, boolean ack) {

        updateCloudletProcessing();
        try {
            // gets the Cloudlet object

            CMSSJob cl = (CMSSJob) ev.getData();
            cl.setTimeCreate(CloudSim.clock());
//            Log.printLine(cl.getCloudletId());
            // tim cho cloudlet mot host ON:
            boolean hasAOnHost = false;
            //tim xem co host nao ON ma ko co jobs nao dang thuc hien tren do ko
            for (Host h : listHostON) {
                if (((CMSSHost) h).getJob() == null) {
                    System.out.println("******** ok");
                    Log.printLine("host "+h.getId()+" chay "+ ((CMSSHost) h).getCurrentvm().getCloudletScheduler().runningCloudlets());
                    jobsqueue.add(cl);
                    jobsqueue.poll();
                    assignJobToHost(cl, h);
                    hasAOnHost = true;

                    break;
                }

            }
            // neu ko tim duoc host ON, chi don gian la dua cloud vao hang doi va bat may tu middle sang ON
            // va bat may tu Middle sang ON
            if (!hasAOnHost) {
                jobsqueue.add(cl);
                if (hasMiddle) checkAndTurnMiddleToOn();
                else checkAndTurnOffToOn();
            }

        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        } catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }

        checkCloudletCompletion();
    }

    protected void checkAndTurnOffToOn() {
        if (hostInSetupMode == null) {
            if (!listHostOff.isEmpty()) {
                Host h = listHostOff.poll();
                turnAOffToON(h);
            }
        }
    }

    protected void turnAOffToON(Host h) {
        hostInSetupMode = h; // cho vao setupmode
        hostSetupNumber.add(new Object());
        verifySetupMode = CloudSim.clock();
//        Log.printLine(CloudSim.clock() + " : host" + h.getId()+" turn from Middle to ON");
        send(getId(),  CMSSHelper.timeOffToMiddle + StdRandom.exp(CMSSHelper.alpha), CMSSConstants.TurnONSuccess, new Double(verifySetupMode)); // bat dau bat

    }

    protected void assignJobToHost(CMSSJob cl, Host host) {

//        Log.printLine("assign job to host" + cl.getCloudletId());
//        Log.printLine("assign job " + cl.getCloudletId() + " to host" + host.getId());
        cl.setHost(host);
        ((CMSSHost) host).setJob(cl);

        // dong sau moi them
        send(getId(),StdRandom.exp(CMSSHelper.muy), CMSSConstants.JobComplete, cl); // bat dau bat



//        // ----------------------
//        Vm vm = ((CMSHost) host).getCurrentvm();
//        cl.setVmId(vm.getId());
//        cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
//                .getCostPerBw());
//        cl.setTimeStartExe(CloudSim.clock());
//
////        if(vm.getCloudletScheduler().runningCloudlets() >0) Log.printLine("-----loi----");
//        double estimatedFinishTime = vm.getCloudletScheduler().cloudletSubmit(cl, 0);
//
//        cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
//                .getCostPerBw());
////        Log.printLine("job  "+ cl.getCloudletId()+"se chay " + estimatedFinishTime);
//        // if this cloudlet is in the exec queue
//        if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
////            estimatedFinishTime += 0;
//            send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
//        }
//
//
////        double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
//
//
////        Vm vm = ((CMSHost) host).getCurrentvm();
////        CloudletScheduler scheduler = vm.getCloudletScheduler();
////        double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);
//
//
//        // if this cloudlet is in the exec queue
////        if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
////            estimatedFinishTime += fileTransferTime;
////            send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
////        }
//
//
////        send(getId(),StdRandom.exp(CMSHelper.muy), CMSConstants.JobComplete, cl); // bat dau bat
//        // --------------------------

    }

    public void checkCloudletCompletion() {

//        // ---------------------
//
////        List<? extends Host> list = getVmAllocationPolicy().getHostList();
//        for (int i = 0; i < listHostON.size(); i++) {
//            Host host = listHostON.get(i);
//
//            Vm vm = ((CMSHost)host).getCurrentvm();
////                Log.printLine("dang chay" +vm.getCloudletScheduler().runningCloudlets());
//                while (vm.getCloudletScheduler().isFinishedCloudlets()) {
//                    Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
//                    if (cl != null) {
//                        processJobComplete((CMSJob) cl);
////                        sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
//
//                    }
//                }
//
//        }
//        //---------------
    }

    protected void turnOffHostInSetupMode() {
        if (hostInSetupMode != null) {
            listHostOff.add(hostInSetupMode);
//            listHostMIDDLE.updateWaitingtime();
            hostInSetupMode = null; hostSetupNumber.poll();
            verifySetupMode = -1;
        }
    }

    // bat may tu Middle sang on co kiem tra cac dieu kien
    protected void checkAndTurnMiddleToOn() {
        if (hostInSetupMode == null) { // kiem tra neu ko co may dang bat tu Middle sang ON


            if (!listHostMIDDLE.isEmpty()) { // kiem tra neu co may o MIDDLE

//                Log.printLine("bat dau bay may o middle sang on");
                Host h = listHostMIDDLE.poll();
                turnAMiddleToON(h);

            }
        }
//        else Log.printLine("co may dang o setupmode nen ko bat tiep duoc");
    }

    protected void turnAMiddleToON(Host h) {
        hostInSetupMode = h; // cho vao setupmode
        hostSetupNumber.add(new Object());

        verifySetupMode = CloudSim.clock();
//        Log.printLine("turn on");
//        Log.printLine(CloudSim.clock() + " : host" + h.getId()+" turn from Middle to ON");
        send(getId(), StdRandom.exp(CMSSHelper.alpha), CMSSConstants.TurnONSuccess, new Double(verifySetupMode)); // bat dau bat
    }

    // xu ly event khi ma turn middle sang on thanh cong
    protected void processTurnONSuccess(SimEvent ev) {

        if (hostInSetupMode != null) {
            double v = ((Double) ev.getData()).doubleValue();
            if (verifySetupMode == v) { // xac nhan su kien co dung la cua host muon bat
                listHostON.add(hostInSetupMode);
                hostOnNumber.add(new Object());

//                deployvminhost(listHostON.get(listHostON.size() - 1));

//                listHostMIDDLE.updateWaitingtime();
                hostInSetupMode = null;
                hostSetupNumber.poll();
//                Log.printLine("so host on: " + listHostON.size() + " so host off:" + listHostOff.size() + " so host middle: " + listHostMIDDLE.getsize());
//                Log.printLine("total host: " + (listHostON.size()+listHostOff.size()+listHostMIDDLE.getsize()));


                // lay 1 jobs trong queue ra de thuc hien tren host nay
                // neu ko co jobs nao thi tat luon may nay
                if (!jobsqueue.isEmpty()) {

                    // thuc hien cloudlet tren host nay


                    assignJobToHost(jobsqueue.poll(), listHostON.get(listHostON.size() - 1));

                    //Kiem tra xem trong queue con job khong neu con thi tiep tuc bat may tu middle sang ON
                    if (!jobsqueue.isEmpty()) {
                        if (hasMiddle) checkAndTurnMiddleToOn();
                        else checkAndTurnOffToOn();
                    }

                } else {
                    // tat luon may on nay
                    turnOffHostOn(listHostON.get(listHostON.size() - 1));
                }

            }

        }
    }

    protected boolean deployvminhost(Host host) {

        Vm vm = CMSSHelper.getVm();
        ((CMSSHost) host).setCurrentvm(vm);
        vm.setHost(host);
        boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

        if (result) {
//            getVmList().add(vm);

            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }

            vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                    .getAllocatedMipsForVm(vm));
        }

        return result;
    }

    protected void turnOffHostOn(Host host) {

//        Log.printLine("tat may");
//        getVmAllocationPolicy().deallocateHostForVm(((CMSHost) host).getCurrentvm());
        listHostON.remove(host);
        hostOnNumber.poll();

        listHostOff.add(host);
    }

    protected void processVmDestroy(SimEvent ev, boolean ack) {

    }

    public double getMeanWaittingTime() {
        return jobsqueue.getMeanWaitingTime();
    }

    public double getMeanJobsQueueLength() {
        return jobsqueue.getMeanQueueLength();
    }

    public double getMeanMiddleServerLength() {
        return listHostMIDDLE.getMeanQueueLength();
    }

    public double getMeanNumberSetupServer(){
        return hostSetupNumber.getMeanQueueLength();
    }

    public double getMeanNumberOnServer(){
        return hostOnNumber.getMeanQueueLength();
    }

    public double getTimeNoMiddle() {
        return listHostMIDDLE.getTimeEmpty();
    }

    public long getTotalJob() {
        return jobsqueue.getTotalItem();
    }
}

