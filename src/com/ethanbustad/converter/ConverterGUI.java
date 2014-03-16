package com.ethanbustad.converter;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

class ConverterGUI extends JFrame {

	public static void main(String[] args) {
		ConverterGUI cgui = new ConverterGUI();

		cgui.setVisible(true);
	}

	public ConverterGUI() {
		setTitle("Converter");
		setSize(300,280);
		setLocation(100,200);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		_addPanel();
	}

	private void _addPanel() {
		Container contentPane = getContentPane();

		contentPane.add(new SimplePanel());
	}

	private class SimplePanel extends JPanel implements ActionListener {

		public SimplePanel() {
			_initElements();
		}

		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();

			if (command.equals(_CONVERT)) {
				String input = _inputField.getText();

				String output = null;

				try {
					output = Converter.parseInput(input);
				}
				catch (Exception e) {
					System.out.println("An error occurred: " + e.getMessage());

					output = _ERROR;
				}

				_addOutput(output);

				_clearField(_inputField);
			}
			else if (command.equals(_CLEAR)) {
				_clearField(_displayArea);
			}
		}

		private void _addOutput(String output) {
			_displayArea.append(output + _NEWLINE);
		}

		private void _clearField(JTextComponent jtc) {
			jtc.setText(null);
		}

		private void _initElements() {
			_label = new JLabel("Enter value to convert, e.g., 6 in to cm: ");
			add(_label);

			_inputField = new JTextField(20);
			add(_inputField);
			_inputField.setActionCommand(_CONVERT);
			_inputField.addActionListener(this);
			_inputField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent ke) {
					if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
						if (_isNotNull(_inputField.getText())) {
							_clearField(_inputField);
						}
						else {
							_clearField(_displayArea);
						}
					}
				}

			});

			_convertButton = new JButton(_CONVERT);
			add(_convertButton);
			_convertButton.addActionListener(this);

			_clearButton = new JButton(_CLEAR);
			add(_clearButton);
			_clearButton.addActionListener(this);

			_displayArea = new JTextArea(8, 20);
			_displayArea.setEditable(false);

			_scrollPane = new JScrollPane(
				_displayArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
			);

			add(_scrollPane);
		}

		private boolean _isNotNull(String s) {
			return ((s != null) && (s.length() > 0));
		}

		private JButton _clearButton;
		private JButton _convertButton;
		private JTextArea _displayArea;
		private JTextField _inputField;
		private JLabel _label;
		private JScrollPane _scrollPane;

		private static final String _CLEAR = "Clear";
		private static final String _CONVERT = "Convert";
		private static final String _ERROR = "Error";
		private static final String _NEWLINE = "\n";

	}

}