package com.beautyteam.everpay.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beautyteam.everpay.Fragments.FragmentAddBill;
import com.beautyteam.everpay.R;
import com.beautyteam.everpay.Views.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 06.04.2015.
 */
public class AddBillListAdapter extends BaseAdapter {

    private ArrayList<BillListItem> billFullArrayList;
    private ArrayList<BillListItem> billAvailableArrayList;
    private HashMap<String, String> mapIdToAvatar = new HashMap<String, String>();
    private Context context;
    private LayoutInflater inflater;
    private int needSumma = 0;
    private int mode=TEXT_VIEW_MODE;

    public static int TEXT_VIEW_MODE = 1;
    public static int EDIT_TEXT_MODE = 2;
    private FragmentAddBill mFragmentAddBill;

    ArrayList<String> investSummaArray = new ArrayList<String>();
    ArrayList<String> needSummaArray = new ArrayList<String>();

    public AddBillListAdapter(Context _context, ArrayList<BillListItem> billFullArrayList, FragmentAddBill fragmentAddBill, String needEditValue, int mode) {
        context = _context;
        this.billFullArrayList = billFullArrayList;
        mFragmentAddBill = fragmentAddBill;

        loadAvatarsFromVK();

        this.mode = mode;
        if (needEditValue.isEmpty())
            needSumma = 0;
        else
            needSumma = Integer.parseInt(needEditValue);

        refreshAvaliableList();
        for (int i=0; i<billAvailableArrayList.size(); i++) {
            investSummaArray.add("");
            needSummaArray.add("");
        }

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void refreshAvaliableList() {
        billAvailableArrayList = new ArrayList<BillListItem>(); // Собираем новый список доступных пользователей
        for (int i = 0; i < billFullArrayList.size(); i++) {
            if (!billFullArrayList.get(i).isRemoved)
                billAvailableArrayList.add(billFullArrayList.get(i));
        }
        if (billAvailableArrayList.size() == billFullArrayList.size()) { // Если хотя бы один удален - показываем кнопку "добавить участника"
            mFragmentAddBill.removeFooterBtn();
        } else {
            mFragmentAddBill.addFooterBtn();
        }
        /*
        Если ввод поля Должен доступен, то определяем дог каждого по общему
         */
        if (mode == TEXT_VIEW_MODE) {
            int needSummaPerUser;
            int count = billAvailableArrayList.size();
            int ostatok = 0;
            if (count != 0) { // чтобы избежать деления на ноль, когда всех пользователей удалили
                needSummaPerUser = needSumma / billAvailableArrayList.size();
                ostatok = needSumma % count;
            }
            else
                needSummaPerUser = 0;

            for (int i=0; i<billAvailableArrayList.size(); i++) {
                int correct = 0;
                if (ostatok != 0) {
                    ostatok--;
                    correct = 1;
                }
                billAvailableArrayList.get(i).need = needSummaPerUser + correct;
            }
        }

        notifyDataSetChanged();
        mFragmentAddBill.setNeedSumma(getNeedSumma());
        mFragmentAddBill.setLeftSumma(getInvestSumma());
    }

    private void loadAvatarsFromVK() {
        String usersId = "";
        int count = billFullArrayList.size();
        for (int i=0; i<count; i++) {
            String vkid = billFullArrayList.get(i).vkid;
            usersId += vkid + ",";
        }

        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, usersId, VKApiConst.FIELDS, "photo_100"));
        VKBatchRequest batch = new VKBatchRequest(request);

        batch.executeWithListener(new VKBatchRequest.VKBatchRequestListener() {
            @Override
            public void onComplete(VKResponse[] responses) {
                super.onComplete(responses);
                VKList<VKApiUser> userList = (VKList<VKApiUser>) responses[0].parsedModel;
                int count = userList.size();
                for (int i = 0; i < count; i++)
                    mapIdToAvatar.put(userList.get(i).id + "", userList.get(i).photo_100);

                notifyDataSetChanged();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkDemoApp", "onError: " + error);
            }
        });
    }



    public int getCountAvailable() {
        return billAvailableArrayList.size();
    }

    @Override
    public int getCount() {
        return billAvailableArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return billAvailableArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.from(context).inflate(R.layout.item_add_bill, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.add_bill_list_name);
            viewHolder.avatar = (RoundedImageView) convertView.findViewById(R.id.add_bill_item_avatar);
            viewHolder.textNeed = (TextView) convertView.findViewById(R.id.add_bill_list_need_text);

            viewHolder.editNeed = (EditText) convertView.findViewById(R.id.add_bill_list_need_edit);
            viewHolder.editNeed.setId(position);
            viewHolder.editNeed.addTextChangedListener(new GenericTextWatcherNeed(viewHolder.editNeed));

            viewHolder.put = (EditText) convertView.findViewById(R.id.add_bill_list_put);
            viewHolder.put.setId(position);
            viewHolder.put.addTextChangedListener(new GenericTextWatcher(viewHolder.put));

            viewHolder.monetize = (ImageView) convertView.findViewById(R.id.add_bill_list_monetize);
            viewHolder.position = position;
            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder)convertView.getTag();
            viewHolder.put.setId(position);
            viewHolder.editNeed.setId(position);

        }

        BillListItem billListItem = (BillListItem)getItem(position);
        viewHolder.name.setText(billListItem.name.replace(" ", "\n") + "");

        viewHolder.textNeed.setText(billListItem.need + "");

        if (billListItem.invest == 0)
            viewHolder.put.setText("");
        else
            viewHolder.put.setText(billListItem.invest + "");

        if (billListItem.need == 0)
            viewHolder.editNeed.setText("");
        else
            viewHolder.editNeed.setText(billListItem.need + "");


        /*
        В зависимости от мода прячем нужные вьюшки
         */
        if (mode == TEXT_VIEW_MODE) {
            viewHolder.textNeed.setVisibility(View.VISIBLE);
            viewHolder.editNeed.setVisibility(View.GONE);
        } else {
            viewHolder.textNeed.setVisibility(View.GONE);
            viewHolder.editNeed.setVisibility(View.VISIBLE);
        }


        String avatarUrl = mapIdToAvatar.get(billAvailableArrayList.get(position).vkid);
        Picasso.with(context)
                .load(avatarUrl)
                .placeholder(context.getResources().getDrawable(R.drawable.default_image))
                .error(context.getResources().getDrawable(R.drawable.default_image))
                .resize(100, 100)
                .centerInside()
                .into(viewHolder.avatar);

        viewHolder.monetize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int diff = getNeedSumma() - getInvestSumma();
                if (diff > 0) {
                    String oldSummaText = viewHolder.put.getText().toString();
                    int oldSummaPut = 0;
                    if (!oldSummaText.isEmpty())
                        oldSummaPut = Integer.parseInt(oldSummaText);
                    int newSummaPut = oldSummaPut + diff;
                    viewHolder.put.setText(newSummaPut + "");
                }
            }
        });

        return convertView;
    }

    public void removeItem(int position) {
        billAvailableArrayList.get(position).isRemoved = true;
        billAvailableArrayList.get(position).invest = 0;
        billAvailableArrayList.get(position).need = 0;
        investSummaArray.remove(position);
        refreshAvaliableList();
    }

    /*
    Считает сумму колонки Внес
     */
    public int getInvestSumma() {
        int summa = 0;
        for (int i=0; i< billAvailableArrayList.size(); i++) {
            summa += billAvailableArrayList.get(i).invest;
        }
        return summa;
    }

    /*
    Считает сумму колонки Должен
     */
    public int getNeedSumma() {
        int summa = 0;
        for (int i=0; i< billAvailableArrayList.size(); i++) {
            summa += billAvailableArrayList.get(i).need;
        }
        return summa;
    }


    private static class ViewHolder {
        TextView name;
        EditText editNeed;
        TextView textNeed;
        EditText put;
        ImageView monetize;
        RoundedImageView avatar;
        int position;
    }

    /*
    Передаем адаптеру информацию о сумме "ДОЛЖНЫ" из фрагмента
     */
    public void setNeedSumma(int summa) {
        needSumma = summa;
        refreshAvaliableList();
    }

    /*
    Устанавливает режим EditText или TextView
     */
    public void setItemMode(int _mode) {
        mode = _mode;
    }

    /*
    Удивительный черный ящик-спаситель!
     */
    private class GenericTextWatcher implements TextWatcher{

        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            final int position = view.getId();
            final EditText editText = (EditText) view;
            String value = editText.getText().toString();
            investSummaArray.set(position, value);
            BillListItem billListItem = billAvailableArrayList.get(position);
            if (value.isEmpty()) billListItem.invest = 0;
            else billListItem.invest = Integer.parseInt(value);
            mFragmentAddBill.setLeftSumma(getInvestSumma());
        }
    }

    /*
    Удивительный черный ящик-спаситель!
     */
    private class GenericTextWatcherNeed implements TextWatcher{

        private View view;
        private GenericTextWatcherNeed(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            final int position = view.getId();
            final EditText editText = (EditText) view;
            String value = editText.getText().toString();
            needSummaArray.set(position, value);
            BillListItem billListItem = billAvailableArrayList.get(position);
            if (value.isEmpty()) billListItem.need = 0;
            else billListItem.need = Integer.parseInt(value);
            mFragmentAddBill.setNeedSumma(getNeedSumma());
        }
    }
}

