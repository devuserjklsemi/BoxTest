package test.boxtest;

import android.content.Context;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.auth.BoxAuthentication.BoxAuthenticationInfo;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxError.ErrorContext;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class BoxClient implements BoxAuthentication.AuthListener {

	private Context context;
	private BoxSession session;
	private String tableFolderId;

	private static final String clientId = "REPLACE_ME";
	private static final String clientSecret = "REPLACE_ME";

	public static boolean isAuthenticated(Context context)
	{
		try
		{
			BoxConfig.IS_LOG_ENABLED = true;
			BoxConfig.CLIENT_ID = clientId;
			BoxConfig.CLIENT_SECRET = clientSecret;
			BoxSession session = new BoxSession(context);
			BoxAuthenticationInfo info = session.getAuthInfo();
			String token = info.accessToken();
            return !(token == null || token.equals(""));
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public static void SignOut(Context context)
	{
		try
		{
			BoxConfig.IS_LOG_ENABLED = true;
			BoxConfig.CLIENT_ID = clientId;
			BoxConfig.CLIENT_SECRET = clientSecret;
			BoxSession session = new BoxSession(context);
			session.logout();
		}
		catch (Exception ex)
		{

		}
	}

	public BoxClient(Context context)
	{
		try
		{
			this.context = context;

			BoxConfig.IS_LOG_ENABLED = true;
			BoxConfig.CLIENT_ID = clientId;
			BoxConfig.CLIENT_SECRET = clientSecret;

			initialize();
		}
		catch (Exception ex)
		{

		}
	}

	private void initialize() {
		try
		{
			session = new BoxSession(context);
			session.setSessionAuthListener(this);
			session.authenticate(context);
		}
		catch (Exception ex)
		{
			int x = 5;
			x++;

		}
	}

	private void getTableFolderStructure()
	{
		String rootFolder = createFolder("0", "ROOT");
		tableFolderId = createFolder(rootFolder, "MY_DATA");
	}


	private String createFolder(String parent, String name)
	{
		try
		{
			BoxApiFolder folderApi = new BoxApiFolder(session);
			return folderApi.getCreateRequest(parent, name).send().getId();
		}
		catch (BoxException ex)
		{
			try
			{
				BoxError error = ex.getAsBoxError();
				ErrorContext ctx = error.getContextInfo();
				ArrayList<BoxEntity> conflicts = ctx.getConflicts();
				if (conflicts.size() > 0)
				{
					return conflicts.get(0).getId();
				}

			}
			catch (Exception e)
			{

			}
		}
		return "";
	}

	private ArrayList<BoxItemInfo> sort(BoxIteratorItems items)
	{
		ArrayList<BoxItemInfo> boxItems = new ArrayList<BoxItemInfo>();
		for (int x=0; x<items.size(); x++)
		{
			boxItems.add(new BoxItemInfo(items.get(x).getId(),items.get(x).getModifiedAt()));
		}
		Collections.sort(boxItems);

		return boxItems;
	}

	public ArrayList<BoxItemInfo> GetItemList()
	{
		try {
			BoxApiFolder folderApi = new BoxApiFolder(session);

			getTableFolderStructure();

			return sort(folderApi.getItemsRequest(tableFolderId).setFields(BoxFolder.FIELD_ID, BoxFolder.FIELD_MODIFIED_AT).send());
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public JSONObject getData(BoxItemInfo item) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			BoxApiFile fileApi = new BoxApiFile(session);
			fileApi.getDownloadRequest(output, item.getId()).send();
			String json = new String(output.toByteArray());
			return new JSONObject(json);
		} catch (Exception e) {
			return null;
		}
		finally
		{
			try {
				output.close();
			}
			catch (IOException ex)
			{

			}
		}
	}


	@Override
	public void onRefreshed(BoxAuthenticationInfo info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthCreated(BoxAuthenticationInfo info) {
		// TODO Auto-generated method stub
		//createFolders();

	}

	@Override
	public void onAuthFailure(BoxAuthenticationInfo info, Exception ex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoggedOut(BoxAuthenticationInfo info, Exception ex) {
		// TODO Auto-generated method stub
		initialize();
	}

}
