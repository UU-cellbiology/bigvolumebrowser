/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class NumberField extends JPanel {

	private double min = Double.NEGATIVE_INFINITY;
	private double max = Double.POSITIVE_INFINITY;

	private boolean integersOnly = false;

	private JTextField textfield;

	private ArrayList<Listener> listener = new ArrayList<>();

	public static interface Listener {
		public void valueChanged(double v);
	}

	public void addListener(Listener l) {
		listener.add(l);
	}

	public void addNumberFieldFocusListener(FocusListener l) {
		textfield.addFocusListener(l);
	}

	public void removeListener(Listener l) {
		listener.remove(l);
	}

	public String getText() {
		return textfield.getText();
	}

	public void setText(String s) {
		textfield.setText(s);
	}
	public void setHorizontalAlignment(int param)
	{
		textfield.setHorizontalAlignment(param);
	}

	private void fireValueChanged(double v) {
		for(Listener l : listener)
			l.valueChanged(v);
	}

	public static void main(String[] args) {
		
		GridBagConstraints gbcNF = new GridBagConstraints();
		gbcNF.weightx = 1.0;
		gbcNF.fill = GridBagConstraints.HORIZONTAL;
		NumberField nf = new NumberField(8, gbcNF);
		nf.setHorizontalAlignment( SwingConstants.RIGHT );
		nf.setText("5.88");
		nf.setLimits(-1000, 1000);
		// nf.setIntegersOnly(true);
		nf.addListener(new Listener() {
			@Override
			public void valueChanged(double v) {
				System.out.println("value changed to " + v);
			}
		});
		//nf.setTFEnabled(false);
		JFrame frame = new JFrame("");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(nf);
		frame.pack();
		frame.setVisible(true);
	}

	public void setLimits(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public void setIntegersOnly(boolean b) {
		this.integersOnly = b;
	}

	private void setTextAndFire(String text) {
		System.out.println("setTextAndFire");
		if(getText().equals(text))
			return;
		setText(text);
		fireValueChanged(Double.parseDouble(text));
	}

	void handleKeyUp() {
		StringBuffer text = new StringBuffer(getText());
		int car = textfield.getCaretPosition();
		int originalCar = car;
		if(car == text.length())
			car--;

		int firstdig = 0;
		for(int i = 0; i < text.length(); i++) {
			if(Character.isDigit(text.charAt(i))) {
				firstdig = i;
				break;
			}
		}

		for(; car >= firstdig; car--) {
			char ch = text.charAt(car);
			if(!Character.isDigit(ch))
				continue;
			int digit = Integer.parseInt(Character.toString(ch));
			if(digit < 9) {
				text.setCharAt(car, Integer.toString(digit + 1).charAt(0));
				setTextAndFire(text.toString());
				textfield.setCaretPosition(originalCar);
				break;
			}
			else if(digit == 9) {
				text.setCharAt(car, '0');
				if(car == firstdig) {
					text.insert(firstdig, '1');
					setTextAndFire(text.toString());
					textfield.setCaretPosition(originalCar + 1);
				}
			}
		}
		double val = Double.parseDouble(getText());
		if(val < min)
			setTextAndFire(Double.toString(min));
		if(val > max)
			setTextAndFire(Double.toString(max));
		if(integersOnly && getText().contains(".")) {
			int intVal = (int)Math.round(Double.parseDouble(getText()));
			setTextAndFire(Integer.toString(intVal));
		}
	}

	void handleKeyDown() {
		StringBuffer text = new StringBuffer(getText());
		int car = textfield.getCaretPosition();
		int originalCar = car;
		if(car == text.length())
			car--;

		int firstdig = 0;
		for(int i = 0; i < text.length(); i++) {
			if(Character.isDigit(text.charAt(i))) {
				firstdig = i;
				break;
			}
		}
		for(; car >= firstdig; car--) {
			char ch = text.charAt(car);
			if(!Character.isDigit(ch))
				continue;
			int digit = Integer.parseInt(Character.toString(ch));
			if(digit > 0) {
				int carP = 0;
				// make 1 -> 0, and 14 -> 4, but only for integers
				if(car == firstdig && digit == 1 && (text.length() > firstdig + 1 && text.charAt(firstdig + 1) != '.')) {
					text.deleteCharAt(firstdig);
					carP = Math.max(0, originalCar - 1);
				}
				// normal case, e.g. 9 -> 8
				else {
					text.setCharAt(car, Integer.toString(digit - 1).charAt(0));
					carP = originalCar;
				}
				if(Double.parseDouble(text.toString()) == 0 && text.charAt(0) == '-') {
					text.deleteCharAt(0);
					carP = Math.max(0, carP - 1);
				}
				setTextAndFire(text.toString());
				textfield.setCaretPosition(carP);
				break;
			}
			else if(digit == 0) {
				// 210 -> 209
				if(car != firstdig)
					text.setCharAt(car, '9');
				// 010 -> 10 (or 010.1 -> 10.1, but not 0.1 -> .1)
				if(car == firstdig && text.length() > firstdig + 1 && text.charAt(firstdig + 1) != '.') {
					text.deleteCharAt(firstdig);
					setTextAndFire(text.toString());
					textfield.setCaretPosition(Math.max(0, originalCar - 1));
					break;
				} else if(car == firstdig && Double.parseDouble(getText()) == 0) {
					String s = getText();
					int cp = originalCar;
					if(s.charAt(0) == '-') {
						s = s.substring(1);
						cp--;
					}
					setText(s);
					textfield.setCaretPosition(cp);
					handleKeyUp();
					setTextAndFire("-" + getText());
					cp++;
					textfield.setCaretPosition(cp);
				} else if(car == firstdig && text.charAt(0) != '-') {
					setTextAndFire("-" + getText());
					textfield.setCaretPosition(originalCar + 1);
				} else if(car == firstdig && text.charAt(0) == '-') {
					setTextAndFire(getText().substring(1));
					textfield.setCaretPosition(Math.max(0, originalCar - 1));
				}
			}
		}
		double val = Double.parseDouble(getText());
		if(val < min)
			setTextAndFire(Double.toString(min));
		if(val > max)
			setTextAndFire(Double.toString(max));
		if(integersOnly && getText().contains(".")) {
			int intVal = (int)Math.round(Double.parseDouble(getText()));
			setTextAndFire(Integer.toString(intVal));
		}
	}
	public void setTFEnabled(boolean enabled)
	{
		textfield.setEnabled(enabled);
	}

	public NumberField(int n,GridBagConstraints gbc) 
	{
		super(new GridBagLayout());
		textfield = new JTextField(n);
		add(textfield, gbc);
		installListeners();
	}
	public NumberField(int n) 
	{
		super(new FlowLayout(0, 0, 0));
		textfield = new JTextField(n);
		add(textfield);
		installListeners();
	}
	
	void installListeners()
	{

		textfield.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int units = e.getWheelRotation();
				double d = Double.parseDouble(getText());
				boolean neg = d < 0;
				if((units > 0 && !neg) || (units < 0 && neg))
					handleKeyUp();
				else if((units < 0 && !neg) || (units > 0 && neg))
					handleKeyDown();
			}
		});

		textfield.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int kc = e.getKeyCode();
				if(kc == KeyEvent.VK_UP) {
					boolean neg = Double.parseDouble(getText()) < 0;
					if(!neg)
						handleKeyUp();
					else
						handleKeyDown();
					e.consume();
				} // VK_UP
				else if(kc == KeyEvent.VK_DOWN) {
					boolean neg = Double.parseDouble(getText()) < 0;
					if(!neg)
						handleKeyDown();
					else
						handleKeyUp();
					e.consume();
				} // VK_DOWN
				else if(kc == KeyEvent.VK_ENTER)
				{
					String input = getText();
					if(input.length()>0)
					{
						fireValueChanged(Double.parseDouble(getText()));
					}
				}
				// fireKeyPressed(e);
			} // keyPressed

			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if((c == '-' && min < 0) || Character.isDigit(c) || (c == '.' && !integersOnly)) {
					 // fireValueChanged(Double.parseDouble(getText() + c));
				} else {
					e.consume();
				}
			}
		});
	}
}
