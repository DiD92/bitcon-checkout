package com.bitpay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A token is used to access the BitPay API and authorize access to a facade to a private key.
 */
public class Token {
	
	private String _guid;
	private long _nonce = 0;
	private String _id = "";
	private String _pairingCode = "";
	private String _facade = "";
	private String _label = "";
	private String _count = "";
	private List<Policy> _policies;
	private String _resource;
	private String _value;
	private String _dateCreated;
    private String pairingExpiration;

    public Token() {}

    // API fields
    //

    @JsonProperty("guid")
	public String getGuid() {
		return _guid;
	}
    
    @JsonProperty("guid")
	public void setGuid(String _guid) {
		this._guid = _guid;
	}
    
    @JsonProperty("nonce")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public long getNonce() {
		return _nonce;
	}
    
    @JsonProperty("nonce")
	public void setNonce(long _nonce) {
		this._nonce = _nonce;
	}

    // Required fields
    //

    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getId() {
		return _id;
	}
    
    @JsonProperty("id")
	public void setId(String _id) {
		this._id = _id;
	}
    
    @JsonProperty("pairingCode")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getPairingCode() {
		return _pairingCode;
	}
    
    @JsonProperty("pairingCode")
	public void setPairingCode(String _pairingCode) {
		this._pairingCode = _pairingCode;
	}

    @JsonProperty("facade")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String getFacade() {
		return _facade;
	}

    @JsonProperty("facade")
    public void setFacade(String _facade) {
		this._facade = _facade;
	}
	
    @JsonProperty("label")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String getLabel() {
		return _label;
	}

    @JsonProperty("label")
    public void setLabel(String _label) {
		this._label = _label;
	}
	
    @JsonProperty("count")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String getCount() {
		return _count;
	}

    @JsonProperty("count")
    public void setCount(String _count) {
		this._count = _count;
	}

    // Response fields
    //

    @JsonIgnore
	public List<Policy> getPolicies() {
		return _policies;
	}
        
    @JsonProperty("policies")
	public void setPolicies(List<Policy> _policies) {
		this._policies = _policies;
	}

    @JsonIgnore
    public String getResource() {
		return _resource;
	}
    
    @JsonProperty("resource")
	public void setResource(String _resource) {
		this._resource = _resource;
	}
	
    @JsonIgnore
	public String getValue() {
		return _value;
	}
    
    @JsonProperty("token")
	public void setValue(String _value) {
		this._value = _value;
	}

    @JsonIgnore
    public String getDateCreated() {
		return _dateCreated;
	}
    
    @JsonProperty("dateCreated")
	public void setDateCreated(String _dateCreated) {
		this._dateCreated = _dateCreated;
	}

    @JsonProperty("pairingExpiration")
    public void setPairingExpiration(String expiration) {
        this.pairingExpiration = expiration;
    }

    @JsonIgnore
    public String getPairingExpiration() {
        return pairingExpiration;
    }

}
