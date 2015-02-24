package net.roybi.SysInfo;

import java.util.ArrayList;

import net.roybi.SysInfo.ui.PageFragment;
import net.roybi.SysInfo.utils.NetworkManager;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.roybi.SysInfo.R;

public class FragmentAccount extends PageFragment {
    ListView mList;
    NetworkManager mConnMgr;
    ArrayList<String> mLst = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mList = (ListView) getView().findViewById(R.id.list);

        mLst.clear();
        final AccountManager accountManager = AccountManager.get(getActivity());
        Account[] accounts = accountManager.getAccountsByType(null);
        for (Account account : accounts) {
            mLst.add(account.toString());
        }

        mList.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mLst));
//        mList.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, getActivity(),
//                        new AccountManagerCallback<Bundle>() {
//                            public void run(AccountManagerFuture<Bundle> future) {
//                                try {
//                                    // If the user has authorized your
//                                    // application to use the tasks API
//                                    // a token is available.
//                                    String token = future.getResult().getString(
//                                            AccountManager.KEY_AUTHTOKEN);
//                                    // Now you can use the Tasks API...
//                                    useTasksAPI(token);
//                                } catch (OperationCanceledException e) {
//                                    // TODO: The user has denied you access to
//                                    // the API, you should handle that
//                                } catch (Exception e) {
//                                    handleException(e);
//                                }
//                            }
//                        }, null);
//            }
//        });
    }

//    public void useTasksAPI(String accessToken) {
//        // Setting up the Tasks API Service
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(
//                accessToken);
//        Tasks service = new Tasks(transport, accessProtectedResource, new JacksonFactory());
//        service.accessKey = INSERT_YOUR_API_KEY;
//        service.setApplicationName("Google-TasksSample/1.0");
//
//        // TODO: now use the service to query the Tasks API
//    }
    
    @Override
    public void onSelected() {
        
    }   
}
