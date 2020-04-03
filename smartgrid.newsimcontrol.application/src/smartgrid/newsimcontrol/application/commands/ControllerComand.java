package smartgrid.newsimcontrol.application.commands;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

import couplingToICT.SimcontrolException;
import smartgrid.newsimcontrol.controller.LocalController;

public abstract class ControllerComand {

	LocalController controller; 
	
	public ControllerComand(LocalController controller) {
		this.controller = controller;
	}
	
	public Object execute(String[] args) throws SimcontrolException, RemoteException, InterruptedException {
		if (!checkArguments(args))
			return doCommand(args);
		else 
			throw new SimcontrolException("The arguments for this command are not in the right form.");
	}
	
	public abstract boolean allow();
	
	public abstract boolean checkArguments(String[] args);
	
	public abstract Object doCommand(String[] args) throws RemoteException, SimcontrolException, InterruptedException;
	
	public Object ReadObjectFromFile(String filepath) {
		 
        try {
 
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
 
            Object obj = objectIn.readObject();
 
            System.out.println("The Object has been read from the file");
            objectIn.close();
            return obj;
 
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
