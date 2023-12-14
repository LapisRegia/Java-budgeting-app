package prjoct;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Gallery_Controller implements Gallery_Interface{
	/** written by:
	 * 		Nur Hafizah binti Ramli
	 *  	80609
	 *  helped by:
	 *  	Dayang Nur Ezaah Adibah binti Abdul Jalil @ Awang Abdul Jalil
	 *  	77921
	 */
	
	private int current_year;
	private int current_month;
	private ArrayList<Image> images = new ArrayList<Image>();
	
	public Gallery_Controller() {
		readFromFile();
	}
	
    public Gallery_Controller(int year , int month) {
    	current_year = year;
    	current_month = month;
    	readFromFile();
    }
    /*	getters	*/
    public int getYear() {
		return current_year;
    }
    public int getMonth() {
    	return current_month;
    }
    public ArrayList<Image> getAll(){
    	return images;
    }
    public Image getSpecific(int index) {
    	return images.get(index);
    }
    public void addEntry(String filePath, String description, float amount, int date) {
    	try {
    			filePath = addImageToDirectory(filePath);
    			System.out.println(filePath);
    			if (!filePath.equals(""))
    			{
    				images.add(new Image(filePath, description, amount, date, current_month, current_year));
        			System.out.println("image added successfully");
    			} else {
    				throw new IOException("add Entry Failed");
    			}
    			
    	}catch (Exception e) {
			e.printStackTrace();
    	}
    }
    public void editEntry(int index, String desc, float amount, int date) {
    	Image temp = images.get(index);
    	temp.setAmount(amount);
    	temp.setDate(date);
    	temp.setDesc(desc);
    	images.set(index, temp);
    }
    /*	wrapper function to delete both the image from the directory and the database*/
    public void deleteEntry(int index) {
    	Image temp = images.get(index);
    	deleteImageFromDirectory(temp.getDir());
    }
    /*	saves the arraylist of images into the images.bin
     * 	Image class implments the Serializable interface for it to be writable as a binary file.*/
    public int writeToFile() {
    	final String filename = "images";
		try {
			FileOutputStream fileOut = new FileOutputStream(filename + ".bin");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(images);
			out.close();
			fileOut.close();
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
    }
    /**
	 * Reads plan logs from a file and populates the images list.
	 * The file name is set as "images.bin".
	 * "why is it surpressed?"
	 * 		answer: casting Object to an arraylist-of-image object isnt exactly safe nor sane.
	 */
    @SuppressWarnings("unchecked")
    public void readFromFile() {
    	final String filename = "images";
		try (ObjectInputStream in = new ObjectInputStream( new FileInputStream(filename + ".bin"))) {
			images = (ArrayList<Image>) in.readObject();
		} catch (EOFException e) {
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
    /*	subfunction that moves the image from the original directory to the destination directory
     *  the images are organised by year/month/image.jpg.
     *  if successful, returns the destination filepath to be stored in the database.*/
    public String addImageToDirectory(String filePath) {
    	try {
    	    File inputFile = new File(filePath);

    	    // Open the image and throw an error if fails
    	    BufferedImage image = ImageIO.read(inputFile);
    	    if (image == null) {
    	        throw new IOException("Failed to open the image file.");
    	    }

    	    // If it's a PNG, reformat it to JPEG
    	    if (filePath.toLowerCase().endsWith(".png")) {
    	        ImageIO.write(image, "jpg", new File(filePath));
    	    }

    	    // Create the destination folder
    	    String destinationFolder = "receipts" + File.separator + current_year + File.separator + current_month;
    	    File folder = new File(destinationFolder);
    	    if (!folder.exists()) {
    	        folder.mkdirs();
    	    }

    	    // Move the file to the destination folder
    	    File destinationFile = new File(folder, inputFile.getName());
    	    inputFile.renameTo(destinationFile);
    	    filePath = destinationFile.getAbsolutePath();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	    filePath ="";
    	}
    	 return filePath;
    }
    /*	takes the image directory of the file to be deleted
     * 	and... deleted the file
     * 	as well as the object in the database.*/
    public void deleteImageFromDirectory(String fileDir) {
    	int index = getImageIndex(fileDir);
    	if (index != -1) {
    		File imageFile = new File(fileDir);
    		images.remove(index);
            if (imageFile.exists()) {
                if (imageFile.delete()) {
                    System.out.println("Image deleted successfully: " + fileDir);
                } else {
                    System.out.println("Failed to delete the image: " + fileDir);
                }
            } else {
                System.out.println("Image file does not exist: " + fileDir);
            }
    	}
    }
    /* returns a temporary array with Images with the same month as the controller*/
    public ArrayList<Image> getImagesInMonth(){
    	ArrayList<Image> temp = new ArrayList<Image>();
    	for (Image i : images) {
    		// if same date is found and fileName
    		if (i.getMonth() == current_month && i.getYear() == current_year) {
    			temp.add(i);
    		}
    	}
    	
    	return temp.isEmpty() ? null : temp;
    }
    /*	gets the image index using the fileDir. why? because the filedir is unique.
     * 	drawbacks: major performance loss
     * */
    public int getImageIndex(String fileDir) {
    	int index = 0;
    	for (Image i : images) {
    		// if same date is found and fileName
    		if (i.getMonth() == current_month && i.getYear() == current_year && i.getDir() == fileDir) {
    			return index;
    		}
    		index += 1;
    	}
    	return -1;
    }
}
