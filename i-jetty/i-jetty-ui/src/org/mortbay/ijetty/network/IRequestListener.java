package org.mortbay.ijetty.network;

import org.json.JSONObject;

public interface IRequestListener {

	public void onComplete(boolean isError, String errMsg, JSONObject respObj);

	public void onError(Exception e);

}
