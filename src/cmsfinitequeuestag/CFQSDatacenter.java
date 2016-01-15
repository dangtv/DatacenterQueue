package cmsfinitequeuestag;



import controlmidleservernostag.StdRandom;
import controlmidleserverstag.CMSSHost;
import controlmidleserverstag.CMSSJob;
import controlmidleserverstag.CMSSMiddleHostQueue;
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
public class CFQSDatacenter extends Datacenter {

    public static int OFF =0;
    public static int WAITING = 1;
    public static int ON = 2;
    public static int INIT = 3;
    public int state = CFQSDatacenter.OFF;
    public int eventNum = 0;

    public CFQSJobsQueue<CMSSJob> jobsqueue = new CFQSJobsQueue<>();
    public static Queue<Host> listHostOff = new LinkedList<>();
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


//    private boolean disableEventCMS = false;
//    private boolean isSubSystem = false;
//    private boolean running = true;  // dung de kich hoat hoac vo hieu subsystem

//    public void setIsSubSystem(boolean isSubSystem) {
//        this.isSubSystem = isSubSystem;
//    }

    public void init(){
        if(state == CFQSDatacenter.INIT) return;
        state = CFQSDatacenter.INIT;
        send(getId(), CFQSHelper.timeOffToMiddle, CFQSConstants.InitSubsystemSuccess, new Integer(eventNum));
    }
    private void processInitSubsystemSuccess(SimEvent ev) {
        int en = ((Integer) ev.getData()).intValue();
        if(en == eventNum) { // kiem tra xem queue da bi tat chua
            state = CFQSDatacenter.WAITING;
            int numberMiddle = (int) (CFQSHelper.timeOffToMiddle / CFQSHelper.timenext);

            for (int i = 0; i < numberMiddle; i++) {
                listHostMIDDLE.add(listHostOff.poll());
            }
            // kiem tra neu hang doi khong rong thi chuyen sang ON luon
            if (!jobsqueue.isEmpty()) toON();
        }
    }
    public void toON() { // from waiting
        // can xet 2 truong hop tu OFF to ON
        // can dong bo neu nhu da datacenter dang chay thi khogn the kich hoat nua
        if(state == CFQSDatacenter.ON) return;
//        Log.printLine(""+ getId()+" "+ getName()+ " duoc kich hoat");
        state = CFQSDatacenter.ON;

        // kich hoat lai qua trinh bat may tu off sang middle
        sendNow(getId(), CFQSConstants.ControlMiddleHostEvent, new Integer(eventNum));
    }

    public void toWAITING(){ // from ON
        if(state == CFQSDatacenter.WAITING) return;
        state = CFQSDatacenter.WAITING;
        eventNum++;
        // can phai viet them de tat het cac may ON, MIDDLE, SETUP ve OFF
        // con oFF->MIDDle thi phai xu ly o cac event bat thanh cong bat thanh cong xong thi tat luon
        //
        listHostOff.addAll(listHostON);
        listHostON.clear();

        // giam so luong MIDDLE di
        int numberofmiddletokeep = (int)(CFQSHelper.timeOffToMiddle / CFQSHelper.timenext);
        while(listHostMIDDLE.getsize() > numberofmiddletokeep){
            listHostOff.add(listHostMIDDLE.poll());
        }
        turnOffHostInSetupMode();

        // **** can update  mean number of server tai day
        // update xong thi xoa het nh?ng biet dem number server d
        while (!hostOnNumber.isEmpty()) hostOnNumber.poll();
        while (!hostSetupNumber.isEmpty()) hostSetupNumber.poll();

    }

    public void toOFForWAITING(){
        // day la xu ly khi hang doi rong
        // can xet xem neu queue nho nhat ma lon hon threshold duoi thi toOFF
        // nguoc la thi toWAITING

        // chu y neu nhu khong con queue khac ngoai queue nay thi phai
        int min = CFQSHelper.jobsqueuecapacity;
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            // duyet cac queue con hoat dong va khac queue nay
            if((temp.state != CFQSDatacenter.OFF) && (temp.getId() != this.getId())) {
                // chi xet nhung queue ma khong bi tat
                // va khac queue nay
                if (temp.getQueueLength() < min) {
                        min = temp.getQueueLength();
                    }
                }
            }
        // kiem tra min nay:
        // chu y truong hop khong con queue nao khac thi min van se la lon nhat va queue ko bi
        // tat ve off
        if(min < CFQSHelper.jobsqueuethresholddown) toOFF();
        else toWAITING();
    }

    public void toOFF(){  // from ON or WAITING
        // tuong tu can dong bo giong nhu enable:
        if(state == CFQSDatacenter.OFF) return;
//        Log.printLine(""+ getId()+" "+ getName()+ " duoc vo hieu");

        // vo hieu subsystem
        state = CFQSDatacenter.OFF;
        // cho nay co van de neu chi de moi running = false lieu da du de vo hieu
        // hoa qua trinh bat may tu off sang middle
        // neu toOFF xong roi lai enable luon thi dan den co 2 event
        // control middle server lien luc dan toi so luong may bat tu off sang
        // middle tang len gap doi ??? giai quyet the nao day???
        // tuc la lam sao phai ngat duoc cai event con chua duoc gui toi
        // ----> them mot bien nua de dong bo
        //
//        disableEventCMS = true;
        eventNum++;

        // can phai viet them de tat het cac may ON, MIDDLE, SETUP ve OFF
        // con oFF->MIDDle thi phai xu ly o cac event bat thanh cong bat thanh cong xong thi tat luon
        //
        listHostOff.addAll(listHostON);
        listHostON.clear();
        while(!listHostMIDDLE.isEmpty()){
            listHostOff.add(listHostMIDDLE.poll());
        }
        turnOffHostInSetupMode();

        // **** can update  mean number of server tai day
        // update xong thi xoa het nh?ng biet dem number server d
        while (!hostOnNumber.isEmpty()) hostOnNumber.poll();
        while (!hostSetupNumber.isEmpty()) hostSetupNumber.poll();
    }

    public void setHasMiddle(boolean hasMiddle) {
        this.hasMiddle = hasMiddle;
    }


    public CFQSDatacenter(String name, DatacenterCharacteristics characteristics,
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

        //listHostOff.addAll(this.getHostList()); phuong thuc nay duoc thay doi
        // de co the su dung chung list host cho nhieu datacenter

        listHostON = vmAllocationPolicy.getHostList(); // ban dau se la rong

//        Log.printLine(""+ getId()+" "+ getName()+" duoc khoi tao");

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
                case CFQSConstants.TurnONSuccess:
                    processTurnONSuccess(ev);
                    break;

                // Resource dynamic info inquiry
                case CFQSConstants.MiddleSucess:
                    processMiddleSuccess(ev);
                    break;
                case CFQSConstants.ControlMiddleHostEvent:
                    controlMiddleHost(ev);
                    break;
                case CFQSConstants.JobComplete:
                    processJobComplete(ev);
                    break;
                case CFQSConstants.InitSubsystemSuccess:
                    processInitSubsystemSuccess(ev);
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

        sendNow(CFQSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, job);
        // neu cloudlet duoc da hoan thanh
        // kiem tra hang doi va tat host:
        if (!jobsqueue.isEmpty()) { // neu co
            // chuyen ngay cloudlet do cho host vua thuc hien xong cloudlet do
//            Log.printLine("host "+ job.getHost().getId()+" da thuc hien xong job "+
//                    job.getCloudletId());
            assignJobToHost(jobsqueue.poll(), job.getHost());
            // kiem tra tiep neu hang doi khong con thi tat may dang trong setup mode
            if (jobsqueue.isEmpty())
            {
                turnOffHostInSetupMode();

//                if(isSubSystem)
                    toOFForWAITING();
            }

        } else { // neu hang doi khong con
            // tat host vua thuc hien xong di
            turnOffHostOn(job.getHost());

            // tat host o trang thai setup di
            turnOffHostInSetupMode();

//            if(isSubSystem)
                toOFForWAITING();
        }


    }


    private void processMiddleSuccess(SimEvent ev) {

        Host h = (Host) ev.getData();

        if( state != CFQSDatacenter.ON) {
            // neu datacenter ko o trang thai ON (la OFF hoac Waiting)
            // tat di
            listHostOff.add(h);
            return;
        }
//        Log.printLine(CloudSim.clock()+ " : host "+h.getId()+" turn to Middle success");
        listHostMIDDLE.add(h);
        if (!jobsqueue.isEmpty()) {

            checkAndTurnMiddleToOn();
        }

        else {
//            if(isSubSystem)
                toOFForWAITING();
        }
    }

    public void controlMiddleHost(SimEvent ev) {
//        if(disableEventCMS) {
//            // neu datacenter bi toOFF
//            // huy viec bat may tuc la huy su kien nay
//            // su kien duoc huy duy nhat mot lan
//            // nho bien disableEventCMS
//            disableEventCMS = false;
//            return;
//        }

        // kiem tra event nay la event moi hay event cu
        if(state == CFQSDatacenter.ON) {
            int et = ((Integer) ev.getData()).intValue();
            if (et == eventNum) { // neu day la event moi
                if (state == CFQSDatacenter.ON) {
                    if (hasMiddle) {
                        double timenext = CFQSHelper.timenext;
                        if (!listHostOff.isEmpty()) {
                            turnAOffToMiddle();
                        }


                        send(getId(), timenext, CFQSConstants.ControlMiddleHostEvent, new Integer(eventNum));
                    }
                }
            }
        }
    }

    protected void turnAOffToMiddle() {
        if (!listHostOff.isEmpty()) {
//            Log.printLine(CloudSim.clock()+" : turn a off server to middle");
            send(getId(), CFQSHelper.timeOffToMiddle, CFQSConstants.MiddleSucess, listHostOff.poll());
        }
    }

    // override xu ly su kien cloudlet moi den:
    public void processCloudletSubmit(SimEvent ev, boolean ack) {

        if(state == CFQSDatacenter.WAITING) toON();
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

//                    Log.printLine("host "+h.getId()+" chay "+
//                            ((CMSHost) h).getCurrentvm().getCloudletScheduler().runningCloudlets());

                    System.out.println("datacenter "+ getName() + "co server ON khong co job");
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
                if(!jobsqueue.add(cl)) // neu hang doi day
                {
                    // tra lai job cho broker
                    // job nay chua duoc set time complete nen no la job chua hoan thanh
                    // broker nhan duoc job nay se xu ly de gui sang datacenter khac
                    sendNow(CFQSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, cl);
                }
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
        send(getId(),  CFQSHelper.timeOffToMiddle + StdRandom.exp(CFQSHelper.alpha), CFQSConstants.TurnONSuccess, new Double(verifySetupMode)); // bat dau bat

    }

    protected void assignJobToHost(CMSSJob cl, Host host) {

//        Log.printLine("assign job to host" + cl.getCloudletId());
//        Log.printLine("assign job " + cl.getCloudletId() + " to host" + host.getId());
        cl.setHost(host);
        ((CMSSHost) host).setJob(cl);

        // dong sau moi them
        send(getId(), StdRandom.exp(CFQSHelper.muy), CFQSConstants.JobComplete, cl); // bat dau bat



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
        send(getId(), StdRandom.exp(CFQSHelper.alpha), CFQSConstants.TurnONSuccess, new Double(verifySetupMode)); // bat dau bat
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
                    else {
//                        if(isSubSystem)
                            toOFForWAITING();
                    }

                } else {
                    // tat luon may on nay
                    turnOffHostOn(listHostON.get(listHostON.size() - 1));

//                    if(isSubSystem)
                        toOFForWAITING();
                }

            }

        }
    }

    protected boolean deployvminhost(Host host) {

        Vm vm = CFQSHelper.getVm();
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



    public int getNumberOfJobLost(){
        return jobsqueue.getNumberJobLost();
    }






    public double getMeanMiddleServerLength() {


//        return listHostMIDDLE.getMeanQueueLength();

        // sua lai nhu sau:
        // duyet tat ca cac datacenter va lay thong tin ve tat ca waiting time
        double totalWaitingTime = 0;
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            totalWaitingTime =totalWaitingTime + temp.getMiddleTotalWaitingTime();

        }
//        Log.printLine("totalWaitingTime  "+ totalWaitingTime);
//        Log.printLine("getWaitingTimeOfQueue   "+this.listHostMIDDLE.start);
        return totalWaitingTime / (CFQSHelper.totalTimeSimulate - CFQSHelper.timeStartSimulate);
    }



    public double getMeanNumberSetupServer(){
//        return hostSetupNumber.getMeanQueueLength();
        double totalWaitingTime = 0;
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            totalWaitingTime = totalWaitingTime + temp.getSetupTotalWaitingTime();

        }

        return totalWaitingTime / (CFQSHelper.totalTimeSimulate - CFQSHelper.timeStartSimulate);
    }

    public double getMeanNumberOnServer(){
//        return hostOnNumber.getMeanQueueLength();
        double totalWaitingTime = 0;
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            totalWaitingTime = totalWaitingTime + temp.getOnTotalWaitingTime();

        }

        return totalWaitingTime / (CFQSHelper.totalTimeSimulate - CFQSHelper.timeStartSimulate);
    }

    public double getOnTotalWaitingTime() {
        return hostOnNumber.getTotalWaitingTime();
    }



    public double getSetupTotalWaitingTime() {
        return hostSetupNumber.getTotalWaitingTime();
    }



    public double getMiddleTotalWaitingTime() {
        return listHostMIDDLE.getTotalWaitingTime();
    }



    public double getTimeNoMiddle() {
        return listHostMIDDLE.getTimeEmpty();
    }

    public long getTotalJob() {
        return jobsqueue.getTotalItem();
    }

    public int getQueueLength(){
        return jobsqueue.getsize();
    }
}

