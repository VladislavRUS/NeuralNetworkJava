import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Net {
    final double alpha = 0.1;

    static boolean needsToBeTrained = false;

    double[] enters;
    double[] hidden;
    double[] out;

    double[][] wEH;
    double[][] wHO;

    double[][] patterns;
    double[][] answers;

    public Net(){
        enters = new double[1024];
        hidden = new double[128];
        out = new double[2];
        wEH = new double[enters.length][hidden.length];
        wHO = new double[hidden.length][out.length];

        if(needsToBeTrained) {
            patterns = new double[20][1024];
            for (int i = 0; i < 20; i++) {
                File file = new File("E:\\numbers\\" + i + ".bmp");
                try {
                    System.out.println("OPENED FILE " + file.getName());
                    BufferedImage image = ImageIO.read(file);
                    int cnt = 0;
                    for (int j = 0; j < image.getWidth(); j++) {
                        for (int k = 0; k < image.getHeight(); k++) {
                            patterns[i][cnt++] = image.getRGB(j, k) == -1? 0.1 : 0.9;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            answers = new double[20][2];
            for (int i = 0; i < 10; i++) {
                answers[i][0] = 1;
                answers[i][1] = 0;
            }

            for (int i = 10; i < 20; i++) {
                answers[i][0] = 0;
                answers[i][1] = 1;
            }

            init();
            study();
            saveWeights();
        }
        else {
            loadWeights();
        }

        File file = new File("E:\\numbers\\21.bmp");
        try {
            BufferedImage image = ImageIO.read(file);
            int cnt = 0;
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    enters[cnt++] = image.getRGB(i, j) == -1 ? 0.1 : 0.9;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        countOut();
        System.out.println("OUT");
        printOut();

        File file2 = new File("E:\\numbers\\22.bmp");
        try {
            BufferedImage image = ImageIO.read(file2);
            int cnt = 0;
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    enters[cnt++] = image.getRGB(i, j) == -1 ? 0.1 : 0.9;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        countOut();
        System.out.println("OUT");
        printOut();

        File file3 = new File("E:\\numbers\\23.bmp");
        try {
            BufferedImage image = ImageIO.read(file3);
            int cnt = 0;
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    enters[cnt++] = image.getRGB(i, j) == -1 ? 0.1 : 0.9;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        countOut();
        System.out.println("OUT");
        printOut();
    }

    private void saveWeights() {
        File f = new File("E:\\net.txt");
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(f));
            for (int i = 0; i < enters.length; i++) {
                for (int j = 0; j < hidden.length; j++) {
                    writer.println(wEH[i][j]);
                }
            }
            writer.flush();
            for (int i = 0; i < hidden.length; i++) {
                for (int j = 0; j < out.length; j++) {
                    writer.println(wHO[i][j]);
                }
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadWeights() {
        File f = new File("E:\\net.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            for (int i = 0; i < enters.length; i++) {
                for (int j = 0; j < hidden.length; j++) {
                    String line = reader.readLine();
                    wEH[i][j] = Double.parseDouble(line);
                }
            }
            for (int i = 0; i < hidden.length; i++) {
                for (int j = 0; j < out.length; j++) {
                    String line = reader.readLine();
                    wHO[i][j] = Double.parseDouble(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(){
        for (int i = 0; i < enters.length; i++) {
            for (int j = 0; j < hidden.length; j++) {
                wEH[i][j] = Math.random() * 0.2 + 0.1;
            }
        }
        for (int i = 0; i < hidden.length; i++) {
            for (int j = 0; j < out.length; j++) {
                wHO[i][j] = Math.random() * 0.2 + 0.1;
            }
        }
    }

    public void countOut(){
        for (int i = 0; i < hidden.length; i++) {
            for (int j = 0; j < enters.length; j++) {
                hidden[i] += enters[j] * wEH[j][i];
            }
            hidden[i] = 1/(1 + Math.exp(-alpha * hidden[i]));
        }

        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < hidden.length; j++) {
                out[i] += hidden[j] * wHO[j][i];
            }
            out[i] = 1/(1 + Math.exp(-alpha * out[i]));
        }
    }

    public void printOut(){
        for (int i = 0; i < out.length; i++) {
            System.out.print(out[i] > 0.5 ? 1 + " " : 0 + " ");
        }
        System.out.println();
    }

    public void study(){
        double error = 0;
        int iterations = 0;
        do{
            for (int p = 0; p < patterns.length; p++) {
                System.arraycopy(patterns[p], 0, enters, 0, enters.length);
                countOut();

                double[] delta_k = new double[out.length];
                for (int i = 0; i < out.length; i++) {
                    delta_k[i] = out[i] * (1 - out[i]) * (out[i] - answers[p][i]);
                }

                error = 0;
                for (int i = 0; i < delta_k.length; i++) {
                    error += delta_k[i] * delta_k[i];
                }
                error /= 2;

                double[] delta_j = new double[hidden.length];
                for (int i = 0; i < hidden.length; i++) {
                    double sum = 0;
                    for (int j = 0; j < out.length; j++) {
                        sum += delta_k[j] * wHO[i][j];
                    }
                    delta_j[i] = hidden[i] * (1 - hidden[i]) * sum;
                }

                for (int i = 0; i < enters.length; i++) {
                    for (int j = 0; j < hidden.length; j++) {
                        wEH[i][j] += -0.01 * delta_j[j] * enters[i];
                    }
                }

                for (int i = 0; i < hidden.length; i++) {
                    for (int j = 0; j < out.length; j++) {
                        wHO[i][j] += -0.01 * delta_k[j] * hidden[i];
                    }
                }
            }
            iterations++;
            if(iterations % 1000 == 0) System.out.println(iterations + " " + error);
        }while (error > 0.00001);
    }

    public static void main(String[] args) {
        new Net();
    }
}
