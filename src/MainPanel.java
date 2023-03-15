import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class MainPanel extends JPanel {
    private String filePath = "";
    private String saveFileDirectory = "";
    private final String filename = "output.jpg";

    private JLabel taskLabel;
    private final JTextArea detailsArea;

    public MainPanel() {
        setLayout(null);

        JLabel pixelRatioButton = new JLabel("Pixel ratio:");
        pixelRatioButton.setBounds(10, 64, 211, 14);
        add(pixelRatioButton);

        JSpinner pixelRatioSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 20, 1));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) pixelRatioSpinner.getEditor()).getTextField();
        tf.setEditable(false);
        pixelRatioSpinner.addChangeListener(e -> updateDetails());
        pixelRatioSpinner.setBounds(120, 62, 60, 20);
        add(pixelRatioSpinner);

        JLabel colorModeLabel = new JLabel("Color Mode:");
        colorModeLabel.setBounds(10, 95, 100, 14);
        add(colorModeLabel);

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton colorRadioButton = new JRadioButton("Full Color");
        buttonGroup.add(colorRadioButton);
        colorRadioButton.setSelected(true);
        colorRadioButton.setBounds(121, 91, 109, 23);
        add(colorRadioButton);

        JRadioButton monochromeRadioButton = new JRadioButton("Monochrome");
        buttonGroup.add(monochromeRadioButton);
        monochromeRadioButton.setBounds(239, 91, 170, 23);
        add(monochromeRadioButton);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);

        JButton openSaveDirectoryButton = new JButton("Open Save Directory");
        openSaveDirectoryButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(saveFileDirectory));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Unable to open given directory.");
            }
        });
        openSaveDirectoryButton.setBounds(310, 230, 160, 23);
        openSaveDirectoryButton.setEnabled(false);
        add(openSaveDirectoryButton);

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(10, 120, 460, 80);
        add(scrollPane);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(e -> {
            try {
                BufferedImage output = pixelateImage(ImageIO.read(new File(filePath))
                        , (int) pixelRatioSpinner.getValue());
                if (monochromeRadioButton.isSelected())
                    output = convertToMonochrome(output);
                ImageIO.write(output, "png", new File(saveFileDirectory + "output.png"));
                taskLabel.setText("Generation Successful!");
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                JOptionPane.showMessageDialog(null, "Unable to generate:\n" + sw);
                taskLabel.setText("Generation Unsuccessful!");
            }
        });
        generateButton.setBounds(10, 202, 460, 23);
        generateButton.setEnabled(false);
        add(generateButton);

        JButton selectImageButton = new JButton("Select Image File");
        selectImageButton.addActionListener(e -> {
            filePath = getImagePath();
            updateDetails();
            generateButton.setEnabled(filePath != null && saveFileDirectory != null);
        });
        selectImageButton.setBounds(10, 11, 211, 42);
        add(selectImageButton);

        JButton selectDirectoryButton = new JButton("Select Save Directory");
        selectDirectoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveFileDirectory = getSaveDirectory();
                updateDetails();
                generateButton.setEnabled(filePath != null && saveFileDirectory != null);
                openSaveDirectoryButton.setEnabled(saveFileDirectory != null);
            }
        });
        selectDirectoryButton.setBounds(259, 11, 211, 42);
        add(selectDirectoryButton);

        taskLabel = new JLabel("");
        taskLabel.setBounds(10, 230, 430, 20);
        add(taskLabel);

        updateDetails();
    }

    private BufferedImage pixelateImage(BufferedImage inputImage, int pixelSize) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int numPixelRows = height / pixelSize;
        int numPixelCols = width / pixelSize;

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < numPixelRows; i++) {
            for (int j = 0; j < numPixelCols; j++) {
                int startX = j * pixelSize;
                int startY = i * pixelSize;
                int endX = startX + pixelSize;
                int endY = startY + pixelSize;
                int avgRed = 0, avgGreen = 0, avgBlue = 0;

                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        Color pixelColor = new Color(inputImage.getRGB(x, y));
                        avgRed += pixelColor.getRed();
                        avgGreen += pixelColor.getGreen();
                        avgBlue += pixelColor.getBlue();
                    }
                }

                avgRed /= (pixelSize * pixelSize);
                avgGreen /= (pixelSize * pixelSize);
                avgBlue /= (pixelSize * pixelSize);

                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        outputImage.setRGB(x, y, new Color(avgRed, avgGreen, avgBlue).getRGB());
                    }
                }
            }
        }
        return outputImage;
    }

    private BufferedImage convertToMonochrome(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
    }

    private void updateDetails() {
        detailsArea.setText("Image path: " + filePath + "\nSave file path: "
                + saveFileDirectory + filename);
    }

    private String getImagePath() {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {
            public String getDescription() {
                return "Image files (.png, .jpg)";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    String fileName = f.getName().toLowerCase();
                    return fileName.endsWith(".png") || fileName.endsWith(".jpg");
                }
            }
        });
        String path = null;
        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION)
            path = (fc.getSelectedFile().getAbsolutePath());
        return path;
    }

    private String getSaveDirectory() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = null;
        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION)
            path = (fc.getSelectedFile().getAbsolutePath());
        return path + "\\";
    }
}