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

import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class BoxClient implements BoxAuthentication.AuthListener {

	private Context context;
	private BoxSession session;
	private String tableFolderId;

	private static final String clientId = "";
	private static final String clientSecret = "";

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

		}
	}

	private void getTableFolderStructure()
	{
		String rootFolder = createFolder("0", "__EpiInfo");
		tableFolderId = createFolder(rootFolder, "Survey_Final");
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
		try {
			BoxApiFile fileApi = new BoxApiFile(session);

			StringBuilder builder = new StringBuilder();

			PipedOutputStream po = new PipedOutputStream();
			PipedInputStream pi = new PipedInputStream(po);

			fileApi.getDownloadRequest(po, item.getId()).send();
			int i;
			po.close();
			InputStreamReader s = new InputStreamReader(pi);

			//GZIPInputStream s = new GZIPInputStream(pi);
			while ((i = s.read()) != -1) {
				builder.append((char) i);
			}
			pi.close();
			s.close();

			return new JSONObject(builder.toString());

		} catch (Exception e) {
			return null;
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
