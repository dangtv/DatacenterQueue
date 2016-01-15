package controlmidleservernostag;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.*;

/**
 * Created by dangbk on 15/04/2015.
 */
public class CMSNSDatacenter extends Datacenter {

    public CMSNSJobsQueue<CMSNSJob> jobsqueue = new CMSNSJobsQueue<>();
    public Queue<CMSNSHost> listHostOff = new LinkedList<>();
//    public Stack<CMSNSHost> listHostOff2 = new Stack<>();
    public CMSNSMiddleHostQueue<CMSNSHost> listHostMIDDLE = new CMSNSMiddleHostQueue<>();

    // them 2 bien nay de tinh luong may trung binh (gia hang doi)
    public CMSNSMiddleHostQueue<Object> hostOnNumber = new CMSNSMiddleHostQueue<>();
    public CMSNSMiddleHostQueue<Object> hostSetupNumber = new CMSNSMiddleHostQueue<>();
    //-----------------

    public List<CMSNSHost> listHostON = new ArrayList<>();
    public List<CMSNSHost> listHostInSetupMode = new ArrayList<>();
    //    private Host hostInOffToOn = null;
    private double alpha;
    private double controlTime;
    private double muy;
    private double lamda;
    private double timeOffToMiddle;
    private double verifySetupMode = -1;



    public CMSNSDatacenter(String name, DatacenterCharacteristics characteristics,
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
        for(int i = 0; i<CMSNSHelper.hostNum;i++){
            listHostOff.add(new CMSNSHost());

        }
//        for(int i =50;i<CMSNSHelper.hostNum;i++) {
//            listHostOff2.add(new CMSNSHost());
//        }
//        listHostON = new Lis;


    }

    public void setStartState(int numberOnServer, int numberMiddleServer){
        for (int i = 0; i< numberOnServer; i++) {
            listHostON.add(listHostOff.poll());

        }
        for(int i = 0; i< numberMiddleServer; i++){
            listHostMIDDLE.add(listHostOff.poll());
        }
    }

    // xu ly su kien moi
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics inquiry
            case CMSNSConstants.TurnONSuccess:
                processTurnONSuccess(ev);
                break;


            case CMSNSConstants.JobComplete:
                processJobComplete(ev);
                break;
            default:
                if (ev == null) {
                    Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
                }
                break;
        }
    }

    private void processJobComplete(SimEvent ev) {
        System.out.println("bat dau ham processJobComplete "+CloudSim.clock());
        CMSNSJob job =(CMSNSJob) ev.getData();
        System.out.println(CloudSim.clock()+ "  event job "+ job.jobid +" complete with time wait: "+ (job.getTimeStartExe() - job.getTimeCreate()) + " in host "+job.getHost().hostid);
        job.getHost().setJob(null);
//                        Log.printLine("so host on: " + listHostON.size() + " so host off:" + listHostOff.size() + " so host middle: " + listHostMIDDLE.getsize());

//                        Log.printLine(CloudSim.clock() + " : cloudlet " + cl.getCloudletId() + " complete");

        sendNow(CMSNSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, job);

        // neu cloudlet duoc da hoan thanh
        // kiem tra hang doi va tat host:
        if (!jobsqueue.isEmpty()) { // neu co
            // chuyen ngay cloudlet do cho host vua thuc hien xong cloudlet do
            assignJobToHost(jobsqueue.poll(), job.getHost());
            // kiem tra tiep neu hang doi khong con thi tat may dang trong setup mode
            turnOffAHostInSetupMode();
        } else { // neu hang doi khong con
            // tat host vua thuc hien xong di
            turnOffHostOn(job.getHost());

            // tat host o trang thai setup di
            turnOffAHostInSetupMode();
        }
        System.out.println("ket thuc ham processJobComplete "+CloudSim.clock());

    }

    protected void updateCloudletProcessing() {

    }
    // override xu ly su kien cloudlet moi den:
    public void processCloudletSubmit(SimEvent ev, boolean ack) {
        System.out.println(CloudSim.clock() + "  event job  " + ((CMSNSJob) ev.getData()).jobid + " submit");

        updateCloudletProcessing();
        try {
            // gets the Cloudlet object
            if(jobsqueue.getsize()+listHostON.size() < 50) {
                CMSNSJob cl = (CMSNSJob) ev.getData();
                // tim cho cloudlet mot host ON:
                boolean hasAOnHost = false;
                //tim xem co host nao ON ma ko co jobs nao dang thuc hien tren do ko
                for (CMSNSHost h : listHostON) {
                    if ( h.getJob() == null) {

                        jobsqueue.add(cl);

                        assignJobToHost(jobsqueue.poll(), h);
                        hasAOnHost = true;
                        break;
                    }

                }
                // neu ko tim duoc host ON, chi don gian la dua cloud vao hang doi va bat may tu middle sang ON
                // va bat may tu Middle sang ON
                if (!hasAOnHost) {
                    jobsqueue.add(cl);
                    System.out.println(CloudSim.clock() + "  event job  " + ((CMSNSJob) ev.getData()).jobid + " submit complete, hang doi co " + jobsqueue.getsize());


                    checkAndTurnOffToOn();
                }
            }
            else {
                System.out.println(CloudSim.clock()+ "  event job  "+ ((CMSNSJob) ev.getData()).jobid +" blocked");

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

    private void checkAndTurnOffToOn() {
        System.out.println("bat dau ham checkAndTurnOffToOn " + CloudSim.clock());
        System.out.println(CloudSim.clock()+ "  check an turn off to on");

        if(listHostInSetupMode.size() < jobsqueue.getsize()) {
            if (!listHostOff.isEmpty()) {
                CMSNSHost h = listHostOff.poll();
                turnAOffToON(h);

            }
            else {
//                if(!listHostOff2.isEmpty()) {
//                    CMSNSHost h = listHostOff2.poll();
//                    turnAOffToON(h);
////
//                }
//                Log.printLine("!!!!!!!!!!!! ko con host OFF");
            }
        }
        System.out.println("ket thuc ham checkAndTurnOffToOn " + CloudSim.clock());
    }

    private void turnAOffToON(CMSNSHost h) {
        System.out.println(CloudSim.clock()+ "  event turn a host OFF to ON bat dau ham " + CloudSim.clock());

        h.isSetUpmode = true;

        listHostInSetupMode.add(h); // cho vao setupmode
        hostSetupNumber.add(new Object());

        double temp = CMSNSHelper.timeOffToMiddle + CMSNSHelper.setuptime.pop().doubleValue();
        System.out.println("bat may tu off on voi thoi gian ---------------------------->  "+ temp);
        h.timeSetupExpect = temp;
        h.timeStartSetup = CloudSim.clock();
        send(getId(), temp, CMSNSConstants.TurnONSuccess, h); // bat dau bat
        System.out.println(CloudSim.clock() + "  event turn a host OFF to ON ket thuc ham " + CloudSim.clock());


    }

    private void assignJobToHost(CMSNSJob cl, CMSNSHost host) {
        System.out.println("bat dau ham assignjobtohost "+CloudSim.clock());
        System.out.println(CloudSim.clock()+ "  assign job " + cl.jobid +" to host " +host.hostid);

        cl.setHost(host);
        host.setJob(cl);
        send(getId(), CMSNSHelper.servicetime.pop().doubleValue(), CMSNSConstants.JobComplete, cl); // bat dau bat
        System.out.println("ket thuc ham assignjobtohost " + CloudSim.clock());


    }

    public void checkCloudletCompletion() {

    }

    private void turnOffAHostInSetupMode() {
        System.out.println(CloudSim.clock()+ "  turnOffAHostInSetupMode bat dau ham "+CloudSim.clock());

        if (listHostInSetupMode.size()>jobsqueue.getsize()) {
            CMSNSHost h = listHostInSetupMode.get(listHostInSetupMode.size() -1);
            h.isSetUpmode = false;
            listHostInSetupMode.remove(h);
            hostSetupNumber.poll();
//            if(listHostOff.size()<50) {

//            System.out.println("truoc khi add listhost off" + CloudSim.clock());

            listHostOff.add(h);
//            System.out.println("sau khi add list host off " + CloudSim.clock());
//            }
//            else {
//                listHostOff2.add(h);
//            }
        }
        System.out.println(CloudSim.clock()+ "  turnOffAHostInSetupMode ket thuc ham "+CloudSim.clock());

    }



    // xu ly event khi ma turn middle sang on thanh cong
    private void processTurnONSuccess(SimEvent ev) {
        System.out.println("bat dau ham process turn on success "+ CloudSim.clock());
        if (listHostInSetupMode.size() > 0) {

            CMSNSHost h = ((CMSNSHost) ev.getData());

            if (((h).isSetUpmode) && ((h.timeStartSetup+h.timeSetupExpect) == CloudSim.clock())) { // xac nhan su kien co dung la cua host muon bat
                System.out.println(CloudSim.clock()+ "  turn on a server success " + ((CMSNSHost) ev.getData()).hostid +
                        " voi thoi gian setup la "+(CloudSim.clock() - h.timeStartSetup) + "  -- so voi thoi gian mong doi "+ h.timeSetupExpect);

                (h).isSetUpmode = false;
                listHostON.add(h); hostOnNumber.add(new Object());
                listHostInSetupMode.remove(h);
                hostSetupNumber.poll();

//                Log.printLine("so host on: " + listHostON.size() + " so host off:" + listHostOff.size() + " so host middle: " + listHostMIDDLE.getsize());
//                Log.printLine("total host: " + (listHostON.size()+listHostOff.size()+listHostMIDDLE.getsize()));



                // lay 1 jobs trong queue ra de thuc hien tren host nay
                // neu ko co jobs nao thi tat luon may nay
                if (!jobsqueue.isEmpty()) {

                    // thuc hien cloudlet tren host nay
                    System.out.println(CloudSim.clock()+ " hang doi ko rong assign job cho host " + ((CMSNSHost) ev.getData()).hostid);

                    assignJobToHost(jobsqueue.poll(), h);

                    //Kiem tra xem trong queue con job khong neu con thi tiep tuc bat may tu middle sang ON
                    if (!jobsqueue.isEmpty()) {

                        checkAndTurnOffToOn();
                    }

                } else {
                    // tat luon may on nay
                    System.out.println(CloudSim.clock()+ " hang doi rong -->tat host " + ((CMSNSHost) ev.getData()).hostid);

                    turnOffHostOn(h);
                }

            }
            else {
                System.out.println("host setup da duoc tat va ko the bat nua");
            }

        }
        System.out.println("ket thuc ham process turn on success "+ CloudSim.clock());

    }

    private void turnOffHostOn(CMSNSHost host) {
        System.out.println(CloudSim.clock()+ "  turnOffHostOn " + host.hostid +" bat dau ham "+CloudSim.clock());
        host.setJob(null);
        listHostON.remove(host);
        hostOnNumber.poll();
//        if(listHostOff.size()<50)
//        System.out.println("truoc khi add listhost off" + CloudSim.clock());

            listHostOff.add(host);
//        System.out.println("sau khi add list host off " + CloudSim.clock());
//        else
//            listHostOff2.add(host);
        System.out.println(CloudSim.clock()+ "  turnOffHostOn " + host.hostid +" ket thuc ham "+CloudSim.clock());

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

    public double getTimeNoMiddle(){
        return listHostMIDDLE.getTimeEmpty();
    }
    public long getTotalJob(){
        return jobsqueue.getTotalItem();
    }
}

