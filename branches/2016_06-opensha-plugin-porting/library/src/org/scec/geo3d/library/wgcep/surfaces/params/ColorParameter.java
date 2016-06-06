package org.scec.geo3d.library.wgcep.surfaces.params;

import java.awt.Color;

import org.dom4j.Element;
import org.opensha.commons.param.AbstractParameter;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.editor.ParameterEditor;

public class ColorParameter extends AbstractParameter<Color> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ColorParameterEditor editor = null;

	public ColorParameter(String name, Color value) {
		super(name, null, null, value);
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		ColorParameter cp = new ColorParameter(getName(), getValue());
		cp.setDefaultValue(getDefaultValue());
		return cp;
	}

	@Override
	public ParameterEditor getEditor() {
		if (editor == null) {
			editor = new ColorParameterEditor(this);
		}
		return editor;
	}

	@Override
	protected boolean setIndividualParamValueFromXML(Element el) {
		// TODO Auto-generated method stub
		return false;
	}

}
