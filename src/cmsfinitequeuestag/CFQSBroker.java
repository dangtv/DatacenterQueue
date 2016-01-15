package cmsfinitequeuestag;


import controlmidleservernostag.StdRandom;
import controlmidleserverstag.CMSSBroker;
import controlmidleserverstag.CMSSJob;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;


/**
 * Created by dangbk on 16/04/2015.
 */
public class CFQSBroker extends CMSSBroker {



    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *             simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public CFQSBroker(String name, double _lamda) throws Exception {

        super(name, _lamda);

    }

    protected void submitCloudlets() {
//        Log.printLine("broker send the cloudlet first");
        sendCloudlet();

        // kich hoat datacenter hoat dong ( bat lien tiep may
        // voi thuat toan control middle servers
        // ham nay chi duoc goi mot lan sau khi broker duoc khoi tao va cloudsim.startsimulation()
        sendNow(CFQSHelper.getMainDatacenterId(), CFQSConstants.ControlMiddleHostEvent, new Integer(0));
    }


//    int tem = 0;
    protected void sendCloudlet() {

//        tem ++;
//        if(tem > 100) return;
//        if(CMSHelper.getCloudletid() > CMSHelper.totalJobs) CloudSim.terminateSimulation();
            CMSSJob cloudlet = CFQSHelper.createJob(getId());
//            Log.printLine(CloudSim.clock() + " : broker send  cloudlet " + cloudlet.getCloudletId());
            sendNow(selectBestDatacenter(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            send(getId(), StdRandom.exp(lamda), CFQSConstants.sendCloudletEvent);
//        }
    }

    public int selectBestDatacenter(){ // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            if(temp.state != CFQSDatacenter.OFF) {
                numbernotOFF ++;
            }
        }
        if(numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if(numbernotOFF ==1) {
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQSDatacenter datanotOFF = null;
            for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
                CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
                if(temp.state != CFQSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if(datanotOFF.getQueueLength() > CFQSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len

        // ------------------ update trang thai cua he thong, tat hoac bat queue moi
        // tim xem co queue nao ko co job ko
        // queue ko co job chi co the la queue waiting
        // vi queue init van the co job neu chua init xong
        CFQSDatacenter datacenternojob = null;

        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            // tim kiem  queue nao hoat dong va ko co job
            if((temp.state != CFQSDatacenter.OFF) && (temp.getQueueLength() == 0)){
                datacenternojob = temp;
                break;
            }
        }

        if(datacenternojob == null){
            // neu ko co queue nao 0 job
            // kiem tra de init mot datacenter moi
            // tim queue max:
            int min = CFQSHelper.jobsqueuecapacity;
            for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
                CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
                // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
                if(temp.state != CFQSDatacenter.OFF) {
                    if (temp.getQueueLength() < min) min = temp.getQueueLength();
                    }
                }
            if(min > CFQSHelper.jobsqueuethresholdup) {
                // tao queue moi
                datacenternojob = CFQSHelper.createSubDatacenter("sub_system");
                datacenternojob.init(); // khoi tao
            }

        }
        else {
            // neu da co roi thi kiem tra de tat no di
            // tim queue min:
            int min = CFQSHelper.jobsqueuecapacity;
            for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
                CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
                // duyet cac queue hoat dong va khac queue no job dang xet
                if((temp.state != CFQSDatacenter.OFF) && (temp.getId() != datacenternojob.getId())) {
                    if (temp.getQueueLength() < min) min = temp.getQueueLength();
                }
            }
            if(min < CFQSHelper.jobsqueuethresholddown) {
                // huy bo queue waiting
                datacenternojob.toOFF();
            }
        }
        // ^^^^^^^^^^^^ xong buoc update trang thai cua he thong


        // --------------tien hanh chon queue phu hop cho job
        CFQSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if((temp.state != CFQSDatacenter.OFF) &&
                    (temp.getQueueLength() < CFQSHelper.jobsqueuecapacity)) {
                // chon ra datacenter co hang doi lon nhat
                if (datacenterBest == null) datacenterBest = temp;
                else {
                    if (temp.getQueueLength()
                            > datacenterBest.getQueueLength())
                        datacenterBest = temp;
                }

            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if(datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            return -1;
            // neu tat ca deu day thi phai anable mot datacneter moi
//            for(int i =0; i< CMSHelper.listSubDatacenter.size();i++){
//                CMSDatacenter temp = CMSHelper.listSubDatacenter.get(i);
//                if(temp.state != CMSDatacenter.ON){ // tim xem co datacenter nao ko chay thi bat
//                    // no len
//                    // anable datacenter nay va tra lai id cua datacenter nay luon
//                    temp.toON();
//                    datacenterBest = temp;
//                    break;  // thoat khoi vong for
//                }
//            }


//            datacenterBest = datacenterWaiting;// neu tat ca da day thi cho job vao queue waiting hoac queue init
//            if(datacenterBest == null) {
//                Log.printLine("*** do dai list data: "+CMSHelper.listSubDatacenter.size());
//                Log.printLine("*** number of ON" + numbernotOFF);
//                Log.printLine("******* ko tim duoc datacenter init or waiting cho job");
//            }
            // neu van chua tim duoc datacenter
            // tao moi:
//            if(datacenterBest == null) {
//                datacenterBest = CMSHelper.createSubDatacenter("sub_system");
//                datacenterBest.toON();
///*
//                if(CMSHelper.listSubDatacenter.size() > 50){
//                    Log.printLine("******** so datacenter la: "+CMSHelper.listSubDatacenter.size()+" *****");
//                    for(int i =0; i< CMSHelper.listSubDatacenter.size();i++) {
//                        Log.print(" " + CMSHelper.listSubDatacenter.get(i).getQueueLength());
//                    }
//                }
//                */
//
//            }


        }

        return datacenterBest.getId();
    }

    protected CFQSDatacenter creatNewDatacenter() {
        // duyet tim datacenter ko hoat dong (OFF)
        CFQSDatacenter newdatacenter = null;
        for(int i =0; i< CFQSHelper.listSubDatacenter.size();i++){
            CFQSDatacenter temp = CFQSHelper.listSubDatacenter.get(i);
            // tim kiem  queue ko nao hoat dong
            if(temp.state == CFQSDatacenter.OFF){
                newdatacenter = temp;
                break;
            }
        }
        if(newdatacenter == null) newdatacenter = CFQSHelper.createSubDatacenter("sub_system");
        return newdatacenter;
    }

    protected void processCloudletReturn(SimEvent ev) {
        CMSSJob cloudlet = (CMSSJob) ev.getData();
//        listJob.add(cloudlet);
//        Log.printLine("da nhan job");

        if(cloudlet.getTimeComplete() == 0) {
            System.out.println("*********  job chua hoan thanh duoc tra lai broker");
            return;
        }

        if(start) {

            numberofjob++;
            if (CloudSim.clock() > CFQSHelper.totalTimeSimulate) {
                Log.printLine(CloudSim.clock());
                CloudSim.terminateSimulation();
            }
//        if(numberofjob > jobbatdau )
            totalwaitingTime = totalwaitingTime - cloudlet.getTimeCreate() + cloudlet.getTimeStartExe();
        }
        else {
            if(CloudSim.clock() > CFQSHelper.timeStartSimulate) start = true;
        }

//        Log.printLine("thoi gian chay thuc te job: "+cloudlet.getCloudletId()+" " + (cloudlet.getTimeComplete()-cloudlet.getTimeStartExe()) + "------ " +
//                "thoi gian chay mong muon  " + cloudlet.getCloudletLength() / CMSHelper.vmMips);

    }


    public double getNumberOfJob(){
        return numberofjob;
    }
}
