package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ImagesSoundQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.IMAGES_SOUND; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("img-mc-01", Topic.IMAGES_SOUND, 2,
                "Which class is used to load an image from a file in Swing?",
                "Image.load()", "BufferedImage", "ImageIO.read()", "Picture",
                "c",
                "ImageIO.read(File) loads an image file (PNG, JPEG, GIF, BMP) and returns a BufferedImage. "
                + "Example: BufferedImage img = ImageIO.read(new File(\"photo.png\")); "
                + "Wrap in try-catch for IOException. To load from a classpath resource: "
                + "ImageIO.read(getClass().getResource(\"/images/sprite.png\"));"),

            mc("img-mc-02", Topic.IMAGES_SOUND, 2,
                "How do you draw an image in paintComponent()?",
                "g.render(image, x, y)",
                "g.drawImage(image, x, y, this)",
                "image.draw(g, x, y)",
                "ImagePainter.draw(g, image, x, y)",
                "b",
                "Graphics.drawImage(Image img, int x, int y, ImageObserver observer) draws the image "
                + "with its top-left at (x, y). Pass 'this' as the observer (the JPanel implements ImageObserver). "
                + "You can also scale: drawImage(img, x, y, width, height, this)."),

            mc("img-mc-03", Topic.IMAGES_SOUND, 2,
                "Which class wraps an image for use as a JLabel or JButton icon?",
                "ImageLabel", "ImageIcon", "IconImage", "SpriteImage",
                "b",
                "ImageIcon wraps an Image and implements the Icon interface used by JLabel and JButton. "
                + "Example: JLabel label = new JLabel(new ImageIcon(\"logo.png\")); "
                + "Or from a BufferedImage: new ImageIcon(bufferedImage)."),

            mc("img-mc-04", Topic.IMAGES_SOUND, 3,
                "Which API plays a short sound clip from a resource file?",
                "Sound.play(\"beep.wav\")",
                "AudioSystem.getClip() loaded with an AudioInputStream, then clip.start()",
                "MediaPlayer.open(\"beep.wav\").play()",
                "System.audio.play(\"beep.wav\")",
                "b",
                "javax.sound.sampled: AudioInputStream ais = AudioSystem.getAudioInputStream(file); "
                + "Clip clip = AudioSystem.getClip(); clip.open(ais); clip.start(); "
                + "For simple sound in games/apps this is the standard Java API. "
                + "clip.loop(Clip.LOOP_CONTINUOUSLY) repeats it."),

            trace("img-tr-01", Topic.IMAGES_SOUND, 2,
                "What is drawn at (10, 20) if img is 100x50 pixels?",
                "BufferedImage img = ImageIO.read(new File(\"sprite.png\"));\n"
                + "// In paintComponent:\n"
                + "g.drawImage(img, 10, 20, this);",
                "The image at (10,20), occupying (10,20) to (110,70)",
                "drawImage with no width/height uses the image's natural size: 100x50. "
                + "Top-left at (10,20), bottom-right at (10+100-1, 20+50-1) = (109, 69)."),

            debug("img-db-01", Topic.IMAGES_SOUND, 3,
                "The image loads as null. Why?",
                "BufferedImage img;\n"
                + "try {\n"
                + "    img = ImageIO.read(new File(\"images/logo.png\"));\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}\n"
                + "// img is null at runtime",
                "ImageIO.read() always returns null for PNG files",
                "The file path 'images/logo.png' is relative to the working directory, which may not be the project root",
                "BufferedImage must be initialised to new BufferedImage() before reading",
                "IOException cannot be caught for image loading",
                "b",
                "Relative paths resolve relative to the JVM's working directory, which in an IDE is often the project root, "
                + "but when running the JAR it may differ. "
                + "Fix: use getClass().getResource(\"/images/logo.png\") to load from the classpath (inside the JAR), "
                + "or use an absolute path, or print the working directory to debug."),

            codegen("img-cg-01", Topic.IMAGES_SOUND, 3,
                "Which correctly loads an image from the classpath and draws it at (0,0)?",
                "Image img = new File(\"/logo.png\"); g.drawImage(img, 0, 0, this);",
                "BufferedImage img = ImageIO.read(getClass().getResource(\"/logo.png\")); g.drawImage(img, 0, 0, this);",
                "ImageIcon icon = new ImageIcon(\"/logo.png\"); g.drawImage(icon, 0, 0, this);",
                "g.drawImage(\"/logo.png\", 0, 0, this);",
                "b",
                "getClass().getResource(\"/logo.png\") loads from the classpath (works inside JARs). "
                + "ImageIO.read returns a BufferedImage. drawImage paints it. "
                + "Option A: File is not an Image. Option C: ImageIcon can't be passed to drawImage directly. "
                + "Option D: drawImage doesn't accept a String path."),

            mc("img-mc-05", Topic.IMAGES_SOUND, 2,
                "How do you scale a BufferedImage when drawing it?",
                "img.scale(0.5)",
                "g.drawImage(img, x, y, newWidth, newHeight, this)",
                "BufferedImage.resize(img, newWidth, newHeight)",
                "g.setImageScale(0.5); g.drawImage(img, x, y, this);",
                "b",
                "drawImage(img, x, y, width, height, observer) scales the image to the specified width and height. "
                + "To scale proportionally: compute the ratio from img.getWidth() and img.getHeight(). "
                + "For higher quality scaling, use Graphics2D with RenderingHints.VALUE_INTERPOLATION_BILINEAR."),

            mc("img-mc-06", Topic.IMAGES_SOUND, 3,
                "What is a BufferedImage.TYPE_INT_ARGB used for?",
                "Images that only support grayscale colors",
                "Images with a 32-bit pixel format including an alpha (transparency) channel",
                "Images that only use 8 colors",
                "Audio buffers stored as image data",
                "b",
                "TYPE_INT_ARGB stores each pixel as a 32-bit int: 8 bits each for alpha, red, green, blue. "
                + "Use it when you need transparency. new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB) "
                + "creates a transparent canvas you can draw on before compositing."),

            mc("img-mc-07", Topic.IMAGES_SOUND, 3,
                "What does clip.loop(Clip.LOOP_CONTINUOUSLY) do for an audio Clip?",
                "Plays the clip once at double speed",
                "Repeats the audio clip indefinitely until clip.stop() is called",
                "Loops the clip exactly 10 times",
                "Creates a loopback test for the audio hardware",
                "b",
                "Clip.LOOP_CONTINUOUSLY = -1, meaning loop forever. "
                + "clip.loop(3) would loop 3 additional times (plays 4 total). "
                + "Use clip.stop() to end playback and clip.setFramePosition(0) to reset to the start."),

            trace("img-tr-02", Topic.IMAGES_SOUND, 3,
                "What are the dimensions of the image drawn on screen?",
                "BufferedImage img = ImageIO.read(new File(\"sprite.png\"));\n"
                + "// sprite.png is 64x64 pixels\n"
                + "// In paintComponent:\n"
                + "g.drawImage(img, 10, 10, 128, 128, this);",
                "128x128 pixels on screen",
                "drawImage with explicit width=128, height=128 scales the 64x64 source to 128x128 on screen. "
                + "The display size is determined by the 4th and 5th arguments, not the image's natural size."),

            trace("img-tr-03", Topic.IMAGES_SOUND, 2,
                "What is img.getWidth() after this code?",
                "BufferedImage img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);",
                "320",
                "The constructor BufferedImage(width, height, type) creates an image with the given pixel dimensions. "
                + "getWidth() returns 320; getHeight() returns 240. The image starts with all pixels set to black (0x000000)."),

            debug("img-db-02", Topic.IMAGES_SOUND, 3,
                "The audio clip plays once but then throws IllegalStateException when called again. Why?",
                "Clip clip = AudioSystem.getClip();\n"
                + "clip.open(audioStream);\n"
                + "clip.start();\n"
                + "// later:\n"
                + "clip.start(); // throws IllegalStateException",
                "Clip can only play once per JVM session",
                "The AudioInputStream was already consumed — you must call clip.setFramePosition(0) before replaying",
                "start() must be called from the main thread only",
                "AudioSystem.getClip() returns a read-only clip",
                "b",
                "After a clip finishes playing, its frame position is at the end. "
                + "Call clip.setFramePosition(0); before clip.start(); to rewind. "
                + "Or use clip.loop(0) which resets position automatically. "
                + "IllegalStateException can also occur if the clip is in the wrong state — check clip.isOpen()."),

            codegen("img-cg-02", Topic.IMAGES_SOUND, 3,
                "Which correctly loops background music from a classpath WAV file?",
                "new Thread(() -> { while(true) Sound.play(\"/music.wav\"); }).start();",
                "try { AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource(\"/music.wav\")); Clip clip = AudioSystem.getClip(); clip.open(ais); clip.loop(Clip.LOOP_CONTINUOUSLY); } catch (Exception e) { e.printStackTrace(); }",
                "SoundPlayer.loop(\"/music.wav\");",
                "MediaPlayer player = new MediaPlayer(\"/music.wav\"); player.setLoop(true); player.play();",
                "b",
                "AudioSystem.getAudioInputStream(URL) loads from classpath resource. "
                + "clip.open(ais) prepares it; clip.loop(LOOP_CONTINUOUSLY) starts looping. "
                + "Option A blocks a thread and creates many resources. "
                + "Options C and D use APIs that don't exist in standard Java SE.")
        );
    }
}
