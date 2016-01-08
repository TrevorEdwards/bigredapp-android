package is.genki.bigredapp.android;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PeopleSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PeopleSearchFragment extends Fragment {

    public PeopleSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PeopleSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PeopleSearchFragment newInstance(String param1, String param2) {
        return new PeopleSearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_people_search, container, false);
        final EditText searchBar = (EditText) view.findViewById(R.id.fragment_people_search_editText);
        ImageButton search = (ImageButton) view.findViewById(R.id.fragment_people_search_button);
        final LinearLayout bigContainer = (LinearLayout) view.findViewById(R.id.fragment_people_search_container);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchedItem = searchBar.getText().toString();
                if(searchedItem.equals(""))
                {
                    Toast.makeText(PeopleSearchFragment.this.getActivity(), "Please enter a NetID", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String url = "https://www.cornell.edu/search/people.cfm?q="+ searchedItem;
                    StringRequest req = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String data) {
                                    Document doc = Jsoup.parse(data);
                                    Elements idk = doc.select("#peoplename");
                                    String name = idk.text();

                                    Elements prof = doc.select("#peopleprofile");
                                    Elements emp = prof.select("#employment1");
                                    Elements typ = prof.select("#generalinfo");
                                    String netid = typ.text();
                                    int dex = netid.indexOf("NETID");
                                    int dex2 = netid.indexOf("EMAIL");
                                    if(netid.equals(""))
                                    {
                                        Toast.makeText(PeopleSearchFragment.this.getActivity(),
                                                "User does not exist", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        String mail = netid.substring(dex2, netid.length());
                                        netid = netid.substring(dex, dex2);
                                        String college = emp.text();
                                        name = name.substring(0, name.length() - 6);
                                        netid = netid.replace("NETID: ", "");

                                        SpannableString spanNameNetid=  new SpannableString(name + " " + netid);
                                        spanNameNetid.setSpan(new RelativeSizeSpan(1.5f), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size

                                        CardView resultCard= (CardView) inflater.inflate(R.layout.card_people_result/* resource id */, bigContainer, false);
                                        ((TextView)resultCard.findViewById(R.id.people_result_card_name))
                                                .setText(spanNameNetid);
                                        ((TextView)resultCard.findViewById(R.id.people_result_card_college))
                                                .setText(college.replace("COLLEGE: ", ""));
                                        ((TextView)resultCard.findViewById(R.id.people_result_card_email))
                                                .setText(mail.replace("EMAIL: ", ""));
                                        bigContainer.addView(resultCard, 1);
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(PeopleSearchFragment.this.getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    SingletonRequestQueue.getInstance(getActivity()).addToRequestQueue(req);
                }
            }
        });

        return view;
    }

}
