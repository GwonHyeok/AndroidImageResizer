package com.hyeok.imageresizer.ui;

import com.hyeok.imageresizer.util.FileDrop;
import com.hyeok.imageresizer.util.ImageResizerUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by GwonHyeok on 15. 2. 8..
 */
public class ImageResizerMain extends JFrame {
    private enum Density {
        MDPI(2), HDPI(3), XHDPI(4), XXHDPI(6), XXXHDPI(8);

        int densityRatio;

        private Density(int densityRatio) {
            this.densityRatio = densityRatio;
        }

        public int getDensityRatio() {
            return this.densityRatio;
        }
    }

    private JPanel rootPanel;
    private JRadioButton hdpiRadioButton;
    private JRadioButton xhdpiRadioButton;
    private JRadioButton xxhdpiRadioButton;
    private JRadioButton xxxhdpiRadioButton;
    private JButton workButton;
    private JTable table1;
    private JScrollPane scrollPane;
    private JTextField textField1;
    private JButton button1;
    private JRadioButton mdpiRadioButton;
    private JLabel text;
    private JLabel text1;
    private JProgressBar progressBar1;
    private JCheckBox showSizeCheckBox;
    private FileListTableModel mFileListModel;
    private Density density;
    private boolean mShowImageSize = true;
    private String ROOT_DIR = "";

    public ImageResizerMain() {
        super.setContentPane(rootPanel);
        super.pack();
        super.setLocationRelativeTo(null);
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setButtonGroup();
        setTable();
        setFileDropListener();
        setWorkButton();

        /* set Root Directory is Current Directory */
        ROOT_DIR = new File(ROOT_DIR).getAbsolutePath();
        textField1.setText(ROOT_DIR);
    }

    /**
     * 현재 이미지 비율을 라디오 버튼에서 가져와
     * 각 해상도별로 이미지 크기를 계산해서 이미지를 각 폴더에 저장
     * MDPI : HDPI : XHDPI : XXHDPI : XXXHDPI
     * 2   :   3   :   4   :   6   :   8
     * <p/>
     * <a href="http://developer.android.com/design/style/iconography.html"></a>
     */
    private void doImageResizeWork() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                workButton.setEnabled(false);
                int progress = 0;
                String imageFolderBase = "drawable-%s";
                progressBar1.setMinimum(0);
                progressBar1.setMaximum(mFileListModel.getFileListItem().size());

                for (FileTableItem item : mFileListModel.getFileListItem()) {

                    BufferedImage bimg;
                    try {
                        bimg = ImageIO.read(item.getFile());
                        int src_width = bimg.getWidth();
                        int src_height = bimg.getHeight();
                        String filename = item.getFile().getName();
                        ROOT_DIR = textField1.getText();

                        for (Density density1 : Density.values()) {
                            /* 만약 같은 해상도 아니면 파일을 만드는 작업 */
                            if (density1 != getCurrentDesnity()) {
                                String densityName = density1.name().toLowerCase();
                                String newName = String.format(imageFolderBase, densityName);
                                String fileDirectory = ROOT_DIR + "/" + newName;

                                File file = new File(fileDirectory);
                                if (file.mkdir() || file.isDirectory()) {
                                    int[] new_size = getReSize(density1, src_width, src_height);
                                    BufferedImage dstImage = ImageResizerUtil.getInstance().createResizedCopy(bimg, new_size[0], new_size[1], false);
                                    ImageIO.write(dstImage, "png", new File(fileDirectory + "/" + filename));
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateProgress(++progress);
                }
                workButton.setEnabled(true);
            }
        }).start();
    }

    private void updateProgress(final int value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar1.setValue(value);
            }
        });
    }

    /**
     * @param density    Want Image Density
     * @param src_width  src Image Width
     * @param src_height src Image Height
     * @return [0] : new Width, [1] : new Height
     */
    private int[] getReSize(Density density, int src_width, int src_height) {
        // 원래 이미지가 XXHDPI 면 이미지 사이즈를 6 으로 나누고 원하는 사이즈의 배율만큼 곱한다.
        int[] reSize = new int[2];
        Density current_density = getCurrentDesnity();
        reSize[0] = (int) ((float) src_width / (float) current_density.getDensityRatio()) * density.getDensityRatio();
        reSize[1] = (int) ((float) src_height / (float) current_density.getDensityRatio()) * density.getDensityRatio();
        return reSize;
    }

    private Density getCurrentDesnity() {
        return this.density;
    }

    private void setWorkButton() {
        workButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doImageResizeWork();
            }
        });
    }

    private void setFileDropListener() {
        new FileDrop(rootPanel, new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                for (File file : files) {
                    if (ImagefileCheck(file.getName())) {
                        mFileListModel.addFileListItem(new FileTableItem(file));
                    }
                }
            }
        });
    }

    /**
     * @param fileName Image File Name
     * @return if is Image return true else false
     */
    private boolean ImagefileCheck(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.contains("png") || fileName.contains("jpeg") || fileName.contains("gif") || fileName.contains("bmp");
    }

    private void setTable() {
        mFileListModel = new FileListTableModel();
        table1.setModel(mFileListModel);
    }

    private void setButtonGroup() {
        ButtonGroup jButtonGroup = new ButtonGroup();
        jButtonGroup.add(mdpiRadioButton);
        jButtonGroup.add(hdpiRadioButton);
        jButtonGroup.add(xhdpiRadioButton);
        jButtonGroup.add(xxhdpiRadioButton);
        jButtonGroup.add(xxxhdpiRadioButton);
        jButtonGroup.setSelected(xxhdpiRadioButton.getModel(), true);
        density = Density.XXHDPI;


        mdpiRadioButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                density = Density.MDPI;
                table1.repaint();
            }
        });

        hdpiRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                density = Density.HDPI;
                table1.repaint();
            }
        });

        xhdpiRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                density = Density.XHDPI;
                table1.repaint();
            }
        });

        xxhdpiRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                density = Density.XXHDPI;
                table1.repaint();
            }
        });

        xxxhdpiRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                density = Density.XXXHDPI;
                table1.repaint();
            }
        });

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(".");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);
                if (fileChooser.showOpenDialog(ImageResizerMain.this) == JFileChooser.APPROVE_OPTION) {
                    ROOT_DIR = fileChooser.getSelectedFile().getAbsolutePath();
                    textField1.setText(ROOT_DIR);
                } else {
                    System.out.println("No Selection ");
                }
            }
        });

        showSizeCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                mShowImageSize = showSizeCheckBox.isSelected();
                repaint();
            }
        });
    }

    private class FileListTableModel extends AbstractTableModel {
        private String[] COLUMN = new String[]{"path", "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
        private ArrayList<FileTableItem> itemArrayList = new ArrayList<FileTableItem>();

        @Override
        public int getRowCount() {
            return itemArrayList.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN.length;
        }

        @Override
        public String getColumnName(int index) {
            return COLUMN[index];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return itemArrayList.get(rowIndex).getFile().getName();
            } else if (columnIndex == 1 || columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5) {
                if (!mShowImageSize) {
                    return "";
                }

                try {
                    BufferedImage image = ImageIO.read(itemArrayList.get(rowIndex).getFile());
                    Density colDensity = null;
                    switch (columnIndex) {
                        case 1:
                            colDensity = Density.MDPI;
                            break;
                        case 2:
                            colDensity = Density.HDPI;
                            break;
                        case 3:
                            colDensity = Density.XHDPI;
                            break;
                        case 4:
                            colDensity = Density.XXHDPI;
                            break;
                        case 5:
                            colDensity = Density.XXXHDPI;
                            break;
                    }
                    int[] resize = getReSize(colDensity, image.getWidth(), image.getHeight());
                    return resize[0] + " x " + resize[1];
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Get ImageSize Error";
                }
            }
            return "Get ImageSize Error";
        }

        private void addFileListItem(FileTableItem fileTableItem) {
            itemArrayList.add(fileTableItem);
            table1.repaint();
        }

        private ArrayList<FileTableItem> getFileListItem() {
            return this.itemArrayList;
        }
    }

    private class FileTableItem {
        private File mFile;

        public FileTableItem(File file) {
            this.mFile = file;
        }

        public File getFile() {
            return mFile;
        }
    }
}
