package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class GuiEventModelQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.GUI_EVENT_MODEL; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("gem-mc-01", Topic.GUI_EVENT_MODEL, 2,
                "What is an ActionListener?",
                "A class that creates buttons",
                "An interface with one method (actionPerformed) called when a user action occurs",
                "A thread that listens for keyboard input",
                "A layout manager that responds to window resize events",
                "b",
                "ActionListener is a functional interface with 'void actionPerformed(ActionEvent e)'. "
                + "It is notified when the user clicks a button or triggers a menu item."),

            mc("gem-mc-02", Topic.GUI_EVENT_MODEL, 2,
                "What method attaches an ActionListener to a JButton?",
                "button.setListener(listener)",
                "button.addActionListener(listener)",
                "button.onClick(listener)",
                "button.registerEvent(listener)",
                "b",
                "addActionListener(ActionListener) registers a listener on the button. "
                + "When clicked, the button calls actionPerformed() on all registered listeners."),

            mc("gem-mc-03", Topic.GUI_EVENT_MODEL, 3,
                "Which thread dispatches Swing events?",
                "The main thread", "A daemon thread", "The Event Dispatch Thread (EDT)", "A thread pool",
                "c",
                "All Swing event callbacks (actionPerformed, mouseClicked, etc.) are invoked on the EDT. "
                + "Never perform long operations in these callbacks — they will freeze the GUI."),

            mc("gem-mc-04", Topic.GUI_EVENT_MODEL, 3,
                "How do you safely update a Swing component from a background thread?",
                "label.setText(value) directly",
                "synchronized(label) { label.setText(value); }",
                "SwingUtilities.invokeLater(() -> label.setText(value))",
                "label.post(value)",
                "c",
                "SwingUtilities.invokeLater() schedules the Runnable to run on the EDT. "
                + "Direct calls from non-EDT threads are unsafe and can cause visual corruption or deadlock."),

            trace("gem-tr-01", Topic.GUI_EVENT_MODEL, 2,
                "What happens when the button is clicked?",
                "JButton btn = new JButton(\"Click Me\");\n"
                + "btn.addActionListener(e -> System.out.println(\"Clicked!\"));\n"
                + "// (button is added to a visible frame)",
                "Clicked!",
                "The lambda is the actionPerformed implementation. When the button is clicked, "
                + "the EDT calls actionPerformed(e), executing System.out.println(\"Clicked!\")."),

            debug("gem-db-01", Topic.GUI_EVENT_MODEL, 3,
                "Why might this listener cause GUI freezing?",
                "button.addActionListener(e -> {\n"
                + "    try { Thread.sleep(3000); } catch (Exception ex) {}\n"
                + "    label.setText(\"Done\");\n"
                + "});",
                "Thread.sleep() is not allowed inside a lambda",
                "label.setText() must be called before Thread.sleep()",
                "Thread.sleep() blocks the EDT for 3 seconds, preventing all GUI updates",
                "addActionListener accepts only anonymous classes, not lambdas",
                "c",
                "actionPerformed runs on the EDT. Sleeping there blocks all event processing "
                + "for 3 seconds — the window becomes unresponsive. "
                + "Use SwingWorker to run the wait in the background, then update the label on the EDT."),

            codegen("gem-cg-01", Topic.GUI_EVENT_MODEL, 2,
                "Which code correctly adds a click handler that prints 'Hello' when a button is pressed?",
                "button.addActionListener(new ActionListener() { void clicked() { System.out.println(\"Hello\"); } });",
                "button.addActionListener(e -> System.out.println(\"Hello\"));",
                "button.onClick(() -> System.out.println(\"Hello\"));",
                "ActionListener.attach(button, () -> System.out.println(\"Hello\"));",
                "b",
                "ActionListener is a functional interface with actionPerformed(ActionEvent). "
                + "A lambda directly implements it. Option A uses wrong method name (clicked, not actionPerformed). "
                + "Options C and D use non-existent Swing API."),

            mc("gem-mc-05", Topic.GUI_EVENT_MODEL, 2,
                "How can you remove an ActionListener from a JButton?",
                "button.clearListeners()",
                "button.removeActionListener(listener)",
                "button.setEnabled(false)",
                "ActionListeners cannot be removed after being added",
                "b",
                "removeActionListener(ActionListener) unregisters a specific listener. "
                + "You must keep a reference to the listener object to remove it later. "
                + "button.getActionListeners() returns an array of currently registered listeners."),

            mc("gem-mc-06", Topic.GUI_EVENT_MODEL, 3,
                "What is a ChangeListener used for?",
                "Listening for changes in a JTextField",
                "Listening for changes in a component's model state (e.g., JSlider value, JTabbedPane selected tab)",
                "Listening for window resize events",
                "Listening for property changes on any object",
                "b",
                "ChangeListener (javax.swing.event) has one method: stateChanged(ChangeEvent e). "
                + "JSlider fires it when the value changes. JTabbedPane fires it when the selected tab changes. "
                + "Use slider.addChangeListener(e -> updateDisplay(slider.getValue()))."),

            mc("gem-mc-07", Topic.GUI_EVENT_MODEL, 3,
                "What does SwingWorker provide?",
                "A way to create multiple JFrames from different threads",
                "A background thread that can safely update Swing components when done",
                "A faster alternative to the Event Dispatch Thread",
                "A thread pool for handling multiple button clicks simultaneously",
                "b",
                "SwingWorker<T,V> executes work in background via doInBackground(), "
                + "then updates the UI in done() which runs on the EDT. "
                + "process(List<V> chunks) is called on the EDT for intermediate results. "
                + "Ideal for long-running tasks like network calls triggered by a button."),

            trace("gem-tr-02", Topic.GUI_EVENT_MODEL, 2,
                "What is the ActionCommand of this button?",
                "JButton btn = new JButton(\"Save\");\n"
                + "System.out.println(btn.getActionCommand());",
                "Save",
                "By default, a JButton's action command is its label text. "
                + "You can change it with btn.setActionCommand(\"save-action\"). "
                + "ActionEvent.getActionCommand() returns this string inside a listener."),

            trace("gem-tr-03", Topic.GUI_EVENT_MODEL, 3,
                "Which method fires when a JSlider value changes?",
                "JSlider slider = new JSlider(0, 100, 50);\n"
                + "slider.addChangeListener(e -> System.out.println(slider.getValue()));\n"
                + "// User drags slider to 75",
                "75",
                "ChangeListener.stateChanged() fires every time the slider moves. "
                + "slider.getValue() returns the current integer value. Output: 75."),

            debug("gem-db-02", Topic.GUI_EVENT_MODEL, 3,
                "The listener fires but e.getSource() is always null. Why?",
                "button.addActionListener(e -> {\n"
                + "    JButton source = (JButton) e.getSource();\n"
                + "    System.out.println(source.getText());\n"
                + "});",
                "getSource() returns null for JButton events",
                "getSource() never returns null for ActionEvents from a JButton — source is the button that fired the event",
                "getSource() returns an ActionEvent, not a JButton",
                "Only named listeners can access getSource()",
                "b",
                "ActionEvent.getSource() returns the Object that fired the event — in this case the JButton. "
                + "It is never null for normally fired events. The cast to JButton is valid. "
                + "getText() returns the button's label. This code is correct."),

            codegen("gem-cg-02", Topic.GUI_EVENT_MODEL, 3,
                "Which uses a single listener for multiple buttons, distinguishing them by action command?",
                "button1.addActionListener(e -> {}); button2.addActionListener(e -> {});",
                "ActionListener listener = e -> { if (\"save\".equals(e.getActionCommand())) save(); else load(); }; button1.setActionCommand(\"save\"); button2.setActionCommand(\"load\"); button1.addActionListener(listener); button2.addActionListener(listener);",
                "ActionListener.setShared(button1, button2, e -> {});",
                "button1.addSharedListener(button2, e -> {});",
                "b",
                "Set distinct action commands on each button, register the same listener on both. "
                + "Inside the listener, switch on e.getActionCommand() to dispatch behavior. "
                + "This avoids duplicating listener code for each button.")
        );
    }
}
