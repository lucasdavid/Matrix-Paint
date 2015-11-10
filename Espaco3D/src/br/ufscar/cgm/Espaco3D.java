package br.ufscar.cgm;

import br.ufscar.cgm.geometria.Aresta3D;
import br.ufscar.cgm.geometria.Face;
import br.ufscar.cgm.geometria.Ponto3D;
import br.ufscar.cgm.preenchimento.ET;
import br.ufscar.cgm.preenchimento.No;
import br.ufscar.cgm.utils.Drawer;
import com.sun.opengl.util.Animator;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

/**
 * Espaco3D.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel)
 * <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */
public class Espaco3D implements GLEventListener {

    Ponto3D posicaoFoco;
    Ponto3D posicaoCamera;
    Ponto3D vetorDirecaoDaCamera;

    Ponto3D vetorDirecaoDaLuz = new Ponto3D(0, 0, 0);
    float intensidadeLuz = -1f;
    float intensidadeLuzAmbiente = -1f;
    float ka = -1f;
    float kd = -1f;

    ArrayList<Face> faces;
    ET tabelaET;
    No AET;

    int[][] buffer;

    public static void main(String[] args) {
        Espaco3D espaco = new Espaco3D();
        Scanner keyboard = new Scanner(System.in);
        keyboard.useLocale(Locale.FRENCH);
        Boolean ready = false;
        while(true){
            try{
                System.out.print("Digite a coordenada X inteira da direcao da luz: ");
                espaco.vetorDirecaoDaLuz.x = keyboard.nextInt();
                System.out.print("Digite a coordenada Y inteira da direcao da luz: ");
                espaco.vetorDirecaoDaLuz.y = keyboard.nextInt();
                System.out.print("Digite a coordenada Z inteira da direcao da luz: ");
                espaco.vetorDirecaoDaLuz.z = keyboard.nextInt();
                if(espaco.vetorDirecaoDaLuz.x == 0 &&
                   espaco.vetorDirecaoDaLuz.y == 0 &&
                   espaco.vetorDirecaoDaLuz.z == 0){
                    System.out.println("Vetor direção não pode ser nulo. Comece de novo.");
                    continue;
                }else 
                    break;
            } catch(Exception e){
                
                System.out.println("Digite o vetor novamente.");
                keyboard.next();
                continue;       
            }
        }
        System.out.println("Utilize virgula para separar casas decimais.");
        while(espaco.intensidadeLuz < 0 || espaco.intensidadeLuz > 1 )
            try {
                System.out.print("Digite um valor entre 0 e 1 da intensidade da luz distante: ");
                espaco.intensidadeLuz = (float) keyboard.nextDouble();
            } catch(Exception e){      
                keyboard.next();
                continue;       
            }
        while(espaco.intensidadeLuzAmbiente < 0 || espaco.intensidadeLuzAmbiente > 1 )
            try {
                System.out.print("Digite um valor entre 0 e 1 da intensidade da luz ambiente: ");
                espaco.intensidadeLuzAmbiente = (float) keyboard.nextDouble();
            } catch(Exception e){      
                keyboard.next();
                continue;       
            }
        while(espaco.kd < 0 || espaco.kd > 1 )
            try {
                System.out.print("Digite um valor entre 0 e 1 da constante de reflexão da luz difusa: ");
                espaco.kd = (float) keyboard.nextDouble();
            } catch(Exception e){      
                keyboard.next();
                continue;       
            }
        while(espaco.ka < 0 || espaco.ka > 1 )
            try {
                System.out.print("Digite um valor entre 0 e 1 da constante de reflexão da luz ambiente: ");
                espaco.ka = (float) keyboard.nextDouble();
            } catch(Exception e){      
                keyboard.next();
                continue;       
            }
        
        Frame frame = new Frame("Simple JOGL Application");
        GLCanvas canvas = new GLCanvas();

        canvas.addGLEventListener(espaco);
        frame.add(canvas);
        frame.setSize(640, 480);
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {

                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        // Center frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {

        GL gl = drawable.getGL();
        System.err.println("INIT GL IS: " + gl.getClass().getName());

        gl.setSwapInterval(1);

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //gl.glShadeModel(GL.GL_FLAT); 
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();

        if (height <= 0) { // avoid a divide by zero error!

            height = 1;
        }

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 2.0, 20.0);
        glu.gluLookAt(-5.0, 6.0, -7.0,
                0.0, 0.0, 0.0,
                0.0, 1.0, 0.0);

        posicaoCamera = new Ponto3D(-5, 6, -7);
        posicaoFoco = new Ponto3D(0, 0, 0);
        vetorDirecaoDaCamera = new Ponto3D(0, 1, 0);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        faces = new ArrayList<Face>();

        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        // Reset the current matrix to the "identity"
        gl.glLoadIdentity();

        Drawer.drawCube(gl, faces, 2, 0, 0, 0);

        //Drawer.drawLine3D(gl, 0, 0, 0, 2, 0, 0);
        //gl.glColor3f(1.0f, 0f, 0f);
        //glut.glutWireCube(1.0f);
        // Flush all drawing operations to the graphics card
        ordenaFaces();
        preencheEspaco3D(gl);
        gl.glFlush();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {

    }

    public void preencheEspaco3D(GL gl) {
        //Nada para preencher
        if (faces == null || faces.size() == 0) {
            return;
        }

        No novoNo;
        boolean paraleloAoEixoZ;
        for (Face f : faces) {
            tabelaET = new ET();
            paraleloAoEixoZ = false;
            novoNo = null;

            for (Aresta3D a : f.arestas) {
                //se a linha não esta desenhada na horizontal
                if (a.inicio.z != a.fim.z) {
                    novoNo = new No(a);
                    tabelaET.adicionaNo(novoNo, Math.min(a.inicio.z, a.fim.z));
                }
            }

            if (novoNo == null) {
                for (Aresta3D a : f.arestas) {
                    if (a.inicio.y != a.fim.y) {
                        Ponto3D p1_yz = new Ponto3D(a.inicio.x, a.inicio.z, a.inicio.y);
                        Ponto3D p2_yz = new Ponto3D(a.fim.x, a.fim.z, a.fim.y);
                        Aresta3D a_yz = new Aresta3D(p1_yz, p2_yz);
                        novoNo = new No(a_yz);
                        tabelaET.adicionaNo(novoNo, Math.min(a.inicio.y, a.fim.y));
                        paraleloAoEixoZ = true;
                    }
                }

            }

            float cor = f.getIntensidade(intensidadeLuzAmbiente, ka,
                    intensidadeLuz, kd, vetorDirecaoDaLuz);
            //System.out.println("Cor = " + cor);
            gl.glColor3f(cor, 0f, 0f);
            preencheFace(gl, paraleloAoEixoZ);
        }

    }

    public void preencheFace(GL gl, boolean paraleloAoZ) {
        No AET = null;

        // TODO implementar min e max de Faces
        int nivel = -Drawer.precision * 10;
        int nivel_max = Drawer.precision * 10;

        //Inicializa AET
        while (tabelaET.isNivelVazio(nivel) && nivel < nivel_max) {
            nivel++;
        }
        //ET está vazia
        if (nivel == nivel_max) {
            return;
        }

        boolean AET_esta_vazia = false;
        No p1, p2;
        while (!AET_esta_vazia && nivel < nivel_max) {
            //AET recebe os nós que ymin = nivel
            if (AET == null) {
                AET = tabelaET.getNivel(nivel);
            } else {
                AET.setUltimoProximo(tabelaET.getNivel(nivel));
            }

            if (AET != null) {
                //System.out.println(nivel + "\n" + AET.toFullString());
            }

            //Remove os nós que ymax = nivel
            //Remove os pontos de ymax no começo da AET
            while (AET != null && AET.getZmax() == nivel) {
                AET = AET.getProximo();
            }
            if (AET == null) {
                AET_esta_vazia = true;
                continue;
            }
            //Remove os pontos de ymax no meio da AET
            p1 = AET;
            p2 = AET.getProximo();
            while (p2 != null) {
                if (p2.getZmax() == nivel) {
                    p1.setProximo(p2.getProximo());
                    p2 = p1.getProximo();
                } else {
                    p1 = p1.getProximo();
                    p2 = p1.getProximo();
                }
            }

            //ordena AET
            AET = No.ordena(AET);

            //preenche figura
            p1 = AET;
            int x1, x2, y1, y2;
            while (p1 != null) {
                //Caso especial
                x1 = p1.getXdoMin().arredondaParaCima();
                y1 = p1.getYdoMin().arredondaParaCima();
                x2 = p1.getProximo().getXdoMin().arredondaParaBaixo();
                y2 = p1.getProximo().getYdoMin().arredondaParaBaixo();
                Drawer.original_size = true;
                if (x1 > x2 && y1 > y2) {
                    if (!paraleloAoZ) {
                        Drawer.drawLine3D(gl, x1, y1, nivel, x1, y1, nivel);
                    } else {
                        Drawer.drawLine3D(gl, x1, nivel, y1, x1, nivel, y1);
                    }
                } else {
                    if (!paraleloAoZ) {
                        Drawer.drawLine3D(gl, x1, y1, nivel, x2, y2, nivel);
                    } else {
                        Drawer.drawLine3D(gl, x1, nivel, y1, x2, nivel, y2);
                    }
                }
                Drawer.original_size = false;

                p1 = p1.getProximo().getProximo();
            }

            //Atualiza o nível
            nivel++;

            //Atualiza o valor dos Nós
            p1 = AET;
            while (p1 != null) {
                p1.setXdoMin(p1.getXdoMin().soma(p1.getDxDz()));
                p1.setYdoMin(p1.getYdoMin().soma(p1.getDyDz()));
                p1 = p1.getProximo();
            }

        }
    }

    private void ordenaFaces() {
        ArrayList<Face> facesOrdenadas = new ArrayList<Face>(faces.size());
        int size = 0;
        int i;
        double d, e;
        for (Face f : faces) {
            d = f.getMaiorDistanciaAtePonto(posicaoCamera);
            for (i = 0; i < size; i++) {
                e = facesOrdenadas.get(i)
                        .getMaiorDistanciaAtePonto(posicaoCamera);
                if (!(e >= d)) {
                    break;
                }
            }
            facesOrdenadas.add(i, f);
            size++;
        }

        faces = facesOrdenadas;

    }

}
