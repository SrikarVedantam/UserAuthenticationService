package co.uk.escape.domain;

import java.util.List;

public interface MessageBundle {

	MessageBase getPayload();

	void setPermissions(List<String> permissions);

	
	
}
