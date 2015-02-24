package net.roybi.SysInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.roybi.SysInfo.ui.PageFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.roybi.SysInfo.R;

public class FragmentSensor extends PageFragment implements OnItemClickListener {
    ListView mList;
    List<Sensor> infos;
    ArrayList<String> mLst = new ArrayList<String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorManager mConnMgr = (SensorManager) getActivity().getSystemService(
                Activity.SENSOR_SERVICE);
        infos = mConnMgr.getSensorList(Sensor.TYPE_ALL);
        if (infos != null) {
            for (Sensor info : infos) {
                mLst.add(info.getName());
            }
        }
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
        mList.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mLst));
        mList.setOnItemClickListener(this);
    }

    @Override
    public void onSelected() {
        
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Field[] fs = Sensor.class.getFields();
        Sensor info = infos.get(position);
        StringBuilder str = new StringBuilder();
        str.append(getString(R.string.sensorname) + info.getName() + "\n");
        str.append(getString(R.string.sensorversion) + info.getVersion() + "\n");
        str.append(getString(R.string.sensortypen) + info.getType() + "\n");
        for (Field f : fs) {
            if (f.getModifiers() == (Modifier.FINAL + Modifier.PUBLIC + Modifier.STATIC)) {
                try {
                    int v = (Integer) f.get(null);
                    if (v == info.getType()) {
                        str.append(getString(R.string.sensortypes) + f.getName() + "\n");
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }

            }
        }

        str.append(getString(R.string.sensorvender) + info.getVendor() + "\n");
        str.append(getString(R.string.sensorpower) + info.getPower() + "\n");
        str.append(getString(R.string.sensorresolution) + info.getResolution() + "\n");
        str.append(getString(R.string.sensormaxrange) + info.getMaximumRange() + "\n");
        new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.app_name))
                .setMessage(str.toString()).create().show();
    }
}
