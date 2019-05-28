package org.scec.vtk.plugins.opensha.obsEqkRup;

import org.dom4j.Element;
import org.opensha.commons.gui.LabeledBorderPanel;
import org.opensha.commons.param.AbstractParameter;
import org.opensha.commons.param.ParamLinker;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.editor.ParameterEditor;

public class RenamedParameterWrapper<E> extends AbstractParameter<E> {
	
	private Parameter<E> param;
	
	public RenamedParameterWrapper(Parameter<E> param, String name) {
		super(name, param.getConstraint(), param.getUnits(), param.getValue());
		this.param = param;
		new ParamLinker<>(param, this);
	}

	@Override
	public ParameterEditor getEditor() {
		ParameterEditor editor = param.getEditor();
		if (editor instanceof LabeledBorderPanel)
			((LabeledBorderPanel)editor).setTitle(getName());
		return editor;
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	protected boolean setIndividualParamValueFromXML(Element arg0) {
		return false;
	}

}
