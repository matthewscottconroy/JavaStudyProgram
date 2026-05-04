package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class SwingComponentsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.SWING_COMPONENTS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("swng-mc-01", Topic.SWING_COMPONENTS, 3,
                "Which component provides a tabbed interface where each tab shows a different panel?",
                "JTabPanel", "JCardLayout", "JTabbedPane", "JTabContainer",
                "c",
                "JTabbedPane lets you add multiple panels, each accessible via a clickable tab. "
                + "Add tabs with: tabbedPane.addTab(\"Title\", panel); "
                + "addTab(String, Icon, Component, String tooltip) adds an icon and tooltip too."),

            mc("swng-mc-02", Topic.SWING_COMPONENTS, 3,
                "What does JMenuBar contain in a Swing application?",
                "Toolbar buttons arranged horizontally",
                "JMenu objects (File, Edit, View...) which in turn contain JMenuItems",
                "Status messages at the top of the window",
                "A list of recently opened files",
                "b",
                "JMenuBar → JMenu (\"File\") → JMenuItem (\"Open\", \"Save\", \"Exit\"). "
                + "Add the menu bar: frame.setJMenuBar(menuBar). "
                + "JMenu can also contain JSeparator and other JMenus (for submenus). "
                + "Add action listeners to JMenuItems for behavior."),

            mc("swng-mc-03", Topic.SWING_COMPONENTS, 3,
                "Which Swing component displays tabular data in rows and columns?",
                "JGrid", "JTable", "JSpreadsheet", "JDataGrid",
                "b",
                "JTable displays data in a grid using a TableModel. "
                + "Simple usage: new JTable(Object[][] data, Object[] columnNames). "
                + "Wrap in JScrollPane so column headers and scroll bars appear: new JScrollPane(table)."),

            mc("swng-mc-04", Topic.SWING_COMPONENTS, 3,
                "What does JScrollPane do when added around a JTable?",
                "Makes the table sortable",
                "Provides scroll bars AND shows the column header row",
                "Adds borders around each cell",
                "Enables cell editing",
                "b",
                "Without JScrollPane, the JTable's column header (JTableHeader) is not visible. "
                + "Wrapping in JScrollPane makes headers visible and adds scroll bars when the table overflows. "
                + "Always use new JScrollPane(table) when adding a JTable to a container."),

            trace("swng-tr-01", Topic.SWING_COMPONENTS, 3,
                "How many tabs does this JTabbedPane have?",
                "JTabbedPane tabs = new JTabbedPane();\n"
                + "tabs.addTab(\"Home\", new JPanel());\n"
                + "tabs.addTab(\"Settings\", new JPanel());\n"
                + "tabs.addTab(\"Help\", new JPanel());",
                "3",
                "Three calls to addTab create three tabs: Home, Settings, and Help."),

            debug("swng-db-01", Topic.SWING_COMPONENTS, 3,
                "The JTable shows no column headers. Why?",
                "JFrame frame = new JFrame();\n"
                + "String[][] data = {{\"Alice\", \"90\"}, {\"Bob\", \"85\"}};\n"
                + "String[] cols = {\"Name\", \"Score\"};\n"
                + "JTable table = new JTable(data, cols);\n"
                + "frame.add(table);  // added directly, not in JScrollPane\n"
                + "frame.setVisible(true);",
                "JTable doesn't support column headers",
                "Column headers require JScrollPane — the JTableHeader is part of the JScrollPane's column header viewport",
                "The column names array must use uppercase strings",
                "frame.add() strips the column header automatically",
                "b",
                "JTable headers are a JTableHeader that JScrollPane places in its column header viewport. "
                + "When you add the JTable directly to the frame, the header has no place to appear. "
                + "Fix: frame.add(new JScrollPane(table));"),

            codegen("swng-cg-01", Topic.SWING_COMPONENTS, 3,
                "Which correctly creates a menu bar with a 'File' menu containing an 'Exit' item?",
                "JMenuBar mb = new JMenuBar(\"File\"); mb.add(new JMenuItem(\"Exit\"));",
                "JMenuBar mb = new JMenuBar(); JMenu file = new JMenu(\"File\"); JMenuItem exit = new JMenuItem(\"Exit\"); exit.addActionListener(e -> System.exit(0)); file.add(exit); mb.add(file); frame.setJMenuBar(mb);",
                "JMenu mb = new JMenu(); mb.add(\"File\"); mb.add(\"Exit\"); frame.add(mb);",
                "MenuBar mb = new MenuBar(); mb.add(new Menu(\"File\")); frame.setMenuBar(mb);",
                "b",
                "Correct hierarchy: JMenuBar > JMenu > JMenuItem. "
                + "Add action listener to the item, add item to menu, add menu to bar, set bar on frame. "
                + "Option D uses AWT (not Swing) MenuBar. "
                + "Option A tries to pass a title String to JMenuBar, which doesn't accept one."),

            mc("swng-mc-05", Topic.SWING_COMPONENTS, 3,
                "What is a JDialog used for?",
                "An alternative to JFrame as the main application window",
                "A secondary window for input or notifications that belongs to a parent window",
                "A floating panel that cannot have menus",
                "A popup that appears inside the JFrame's content pane",
                "b",
                "JDialog creates modal or modeless secondary windows. "
                + "Modal: blocks interaction with the parent until closed (new JDialog(parent, \"Title\", true)). "
                + "Use for confirmations, forms, and alerts. JOptionPane.showMessageDialog() uses JDialog internally."),

            mc("swng-mc-06", Topic.SWING_COMPONENTS, 2,
                "Which static JOptionPane method shows a simple message dialog?",
                "JOptionPane.showDialog(parent, message)",
                "JOptionPane.showMessageDialog(parent, message)",
                "JOptionPane.alert(parent, message)",
                "JOptionPane.display(message)",
                "b",
                "JOptionPane.showMessageDialog(parentComponent, message) shows an informational popup. "
                + "showConfirmDialog returns YES/NO/CANCEL. showInputDialog lets the user type text. "
                + "These are convenience wrappers around JDialog."),

            mc("swng-mc-07", Topic.SWING_COMPONENTS, 3,
                "What does JFileChooser allow the user to do?",
                "Browse and select files or directories from the filesystem via a GUI dialog",
                "Edit files directly inside a Swing panel",
                "Upload files to a remote server",
                "Display file contents in a JTextArea",
                "a",
                "JFileChooser shows a system-style file browser. "
                + "int result = chooser.showOpenDialog(parent); "
                + "if (result == JFileChooser.APPROVE_OPTION) File f = chooser.getSelectedFile(); "
                + "Use showSaveDialog() for save dialogs."),

            trace("swng-tr-02", Topic.SWING_COMPONENTS, 3,
                "What does the user see when this code runs?",
                "JOptionPane.showMessageDialog(null, \"File saved!\", \"Success\", JOptionPane.INFORMATION_MESSAGE);",
                "A dialog box titled 'Success' with the message 'File saved!' and an information icon",
                "showMessageDialog with INFORMATION_MESSAGE shows the standard blue 'i' icon. "
                + "The null parent centers it on screen. The user must click OK to dismiss it."),

            trace("swng-tr-03", Topic.SWING_COMPONENTS, 3,
                "What integer does showConfirmDialog return when the user clicks 'No'?",
                "int result = JOptionPane.showConfirmDialog(null, \"Are you sure?\");",
                "JOptionPane.NO_OPTION (value 1)",
                "showConfirmDialog returns: YES_OPTION=0, NO_OPTION=1, CANCEL_OPTION=2, CLOSED_OPTION=-1. "
                + "Check: if (result == JOptionPane.YES_OPTION) to branch on confirmation."),

            debug("swng-db-02", Topic.SWING_COMPONENTS, 3,
                "The JDialog blocks the main window even though it should be non-modal. Why?",
                "JDialog dialog = new JDialog(frame, \"Info\", true);  // true = modal\n"
                + "dialog.add(new JLabel(\"Processing...\"));\n"
                + "dialog.pack();\n"
                + "dialog.setVisible(true);",
                "JDialog is always modal by default",
                "The third constructor parameter is 'true' for modal — change to 'false' for non-modal",
                "setVisible(true) always blocks on JDialog",
                "Only JFrame can be non-modal",
                "b",
                "new JDialog(parentFrame, title, modal) — the boolean controls modality. "
                + "true = modal (blocks the parent). false = modeless (parent remains interactive). "
                + "Change to: new JDialog(frame, \"Info\", false);"),

            codegen("swng-cg-02", Topic.SWING_COMPONENTS, 3,
                "Which correctly shows a file chooser and prints the selected file path?",
                "File f = JFileChooser.getFile(); System.out.println(f.getPath());",
                "JFileChooser chooser = new JFileChooser(); if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) System.out.println(chooser.getSelectedFile().getAbsolutePath());",
                "JFileChooser.open(frame, f -> System.out.println(f));",
                "File f = new FileDialog(frame).getFile(); System.out.println(f);",
                "b",
                "showOpenDialog returns an int; compare to APPROVE_OPTION to check if the user confirmed. "
                + "getSelectedFile() returns the chosen File. getAbsolutePath() gives the full path. "
                + "Option D uses AWT FileDialog, not Swing JFileChooser.")
        );
    }
}
