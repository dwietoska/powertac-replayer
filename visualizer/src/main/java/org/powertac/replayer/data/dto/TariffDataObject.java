package org.powertac.replayer.data.dto;

import org.powertac.common.TariffSpecification;

/**
 * Contains data that are required for the tariff specification view in 
 * the visualizer. Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class TariffDataObject {

	/**
	 * Tariff specification.
	 */
	private TariffSpecification spec;
	
	/**
	 * Tariff data attributes.
	 */
	private TariffDataAttributes tariffDataAttributes;

	/**
	 * Creates new TariffDataObject.
	 */
	public TariffDataObject() {
		
	}
	
	/**
	 * Creates new TariffDataObject.
	 * 
	 * @param spec TariffSpecification
	 * @param tariffDataAttributes TariffDataAttributes
	 */
	public TariffDataObject(TariffSpecification spec, 
			TariffDataAttributes tariffDataAttributes) {
		super();
		this.spec = spec;
		this.tariffDataAttributes = tariffDataAttributes;
	}

	/** Getter and Setter and toString. */
	public TariffSpecification getSpec() {
		return spec;
	}

	public void setSpec(TariffSpecification spec) {
		this.spec = spec;
	}

	public TariffDataAttributes getTariffDataAttributes() {
		return tariffDataAttributes;
	}

	public void setTariffDataAttributes(TariffDataAttributes 
			tariffDataAttributes) {
		this.tariffDataAttributes = tariffDataAttributes;
	}

	@Override
	public String toString() {
		return "TariffDataObject [spec=" + spec + ", tariffDataAttributes="
				+ tariffDataAttributes + "]";
	}
}
