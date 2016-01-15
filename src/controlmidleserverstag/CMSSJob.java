package controlmidleserverstag;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Created by dangbk on 20/04/2015.
 */
public class CMSSJob extends Cloudlet{
    protected double timeCreate = 0;
    protected double timeStartExe = 0;
    protected double timeComplete = 0;
    Host host = null;


    public CMSSJob(int i, long length, int pesNumber,
                   long fileSize, long outputSize,
                   UtilizationModel utilizationModelCPU,
                   UtilizationModel utilizationModelRAM,
                   UtilizationModel utilizationModelBW) {
        super(i, length, pesNumber, fileSize, outputSize,
                utilizationModelCPU, utilizationModelRAM,
                utilizationModelBW);
        timeCreate = CloudSim.clock();

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

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
}
