package controlmidleserverstag;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CMSSVmAllocationPolicy extends VmAllocationPolicySimple {

    /**
     * Creates the new VmAllocationPolicySimple object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    public CMSSVmAllocationPolicy(List<? extends Host> list) {
        super(list);
    }
    public boolean allocateHostForVm(Vm vm) {
        boolean result = false;

        Host host = null;
//        host = getHostList().get(getHostList().size()-1);
        host = vm.getHost();
        result = host.vmCreate(vm);
        if (result) { // if vm were succesfully created in the host
            getVmTable().put(vm.getUid(), host);
//            Log.printLine("deploy vm success");
        }


        return result;



    }
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        int idx = getHostList().indexOf(host);
//        int pes = getUsedPes().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
//            getFreePes().set(idx, getFreePes().get(idx) + pes);
        }
    }
}
