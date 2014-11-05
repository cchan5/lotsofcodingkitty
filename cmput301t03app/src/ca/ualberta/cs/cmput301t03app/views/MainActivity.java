/*
 * This class is the main access point to the application. 
 * 
 * @author
 * 
 * */
package ca.ualberta.cs.cmput301t03app.views;

import java.util.ArrayList;
import ca.ualberta.cs.cmput301t03app.R;
import ca.ualberta.cs.cmput301t03app.adapters.MainListAdapter;
import ca.ualberta.cs.cmput301t03app.controllers.PostController;
import ca.ualberta.cs.cmput301t03app.mockserver.mockServer;
import ca.ualberta.cs.cmput301t03app.mockserver.mockServerDataManager;
import ca.ualberta.cs.cmput301t03app.models.Question;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	ListView lv;
	MainListAdapter mla;
	PostController pc = new PostController(this);
	public AlertDialog alertDialog1; //for testing purposes
	private mockServerDataManager mockServerManage; // FOR TESTING PURPOSES ONLY

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView questionList = (ListView) findViewById(R.id.activity_main_question_list);
		questionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
				Log.d("click", "click " + position);
				toQuestionActivity(position);
			}
		});
		
		questionList.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent,View view, int position, long id) {{
					// Task task = todoList.get(position);
					addToToRead(position);
				}
				return true;
			}
		});
		
		setupAdapter();
		
		/* ----FOR TESTING PURPOSES ONLY-----*/
		mockServerManage = new mockServerDataManager(this);
		mockServer.initServer(this);
		ArrayList<Question> qList = mockServer.getMainList();
		for (int i=0; i < qList.size(); i++) {
			pc.getQuestionsInstance().add(qList.get(i));
		}
		mla.updateAdapter(pc.getQuestionsInstance());
		/*----- END TEST BLOCK-------*/
		 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mla.updateAdapter(pc.getQuestionsInstance());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {return true;}
		if (id == R.id.user_home) {
			Intent intent = new Intent(this, UserHome.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	public void addQuestionButtonFunction(View view) {
		// Pops up dialog box for adding a question
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.activity_respond, null);// Get XML file to view
		final EditText questionTitle = (EditText) promptsView
				.findViewById(R.id.questionTitle);
		final EditText questionBody = (EditText) promptsView
				.findViewById(R.id.questionBody);
		final EditText userName = (EditText) promptsView
				.findViewById(R.id.UsernameRespondTextView);
		
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);// Create a new AlertDialog

		
		alertDialogBuilder.setView(promptsView);// Link the alertdialog to the XML
		alertDialogBuilder.setPositiveButton("Ask!",
				new DialogInterface.OnClickListener() {
					@Override// Building the dialog for adding
					public void onClick(DialogInterface dialog, int which) {

						String questionTitleString = (String) questionTitle
								.getText().toString();
						String questionBodyString = (String) questionBody
								.getText().toString();
						String userNameString = (String) userName.getText()
								.toString();

						Question q = new Question(questionTitleString,
								questionBodyString, userNameString);
						pc.getQuestionsInstance().add(q);

						mla.updateAdapter(pc.getQuestionsInstance());
						pc.addUserPost(q);
						
						/* THIS IS FOR TESTING VIA MOCK SERVER
						 */
						mockServerManage.mockPushQuestionToServer(q);
						/* END OF TEST MOCK SERVER TEST BLOCK
						 */
						
					}

				}).setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing
						dialog.cancel();
					}
				});
		
		final AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog1 = alertDialog;
		alertDialog.show();
		alertDialog.getButton(AlertDialog.BUTTON1).setEnabled(false);		
		

		TextWatcher textwatcher = new TextWatcher(){		
			//creating a listener to see if any changes to edit text in dialog
			private void handleText(){
				final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				if(questionTitle.getText().length() == 0){	//these checks the edittext to make sure not empty edit text
					button.setEnabled(false);
				}
				else if(questionBody.getText().length() == 0){
					button.setEnabled(false);
				}
				else if(userName.getText().length() == 0){
					button.setEnabled(false);
				}
				else{
					button.setEnabled(true);
				}	
			}
			@Override
			public void afterTextChanged(Editable s) {
				handleText();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				//do nothing
			}
			@Override
			public void onTextChanged(CharSequence s, int start,		
					int before, int count) {
				//do nothing
			}
		};
			
		questionTitle.addTextChangedListener(textwatcher);	//adding listeners to the edittexts
		questionBody.addTextChangedListener(textwatcher);
		userName.addTextChangedListener(textwatcher);
		Toast.makeText(this, "Please write your question", Toast.LENGTH_SHORT)
				.show();
	}
	
	public void addToToRead(final int position) {
		/**Brings up Dialog window with options to Archive and Delete
		*http://developer.android.com/reference/android/app/AlertDialog.Builder.html#setSingleChoiceItems(java.lang.CharSequence[],
		*int, android.content.DialogInterface.OnClickListener)
		*Edit existing question with LONG CLICK
		*/
		AlertDialog.Builder editDialog = new AlertDialog.Builder(this);
		editDialog
			.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
			.setPositiveButton("Add to To-Read",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						pc.addToRead(pc.getQuestionsInstance().get(position));
					}
				});
		
		AlertDialog alertDialog = editDialog.create();
		alertDialog.show();
	}
	
	public AlertDialog getDialog(){		//this is for testing purposes
		return alertDialog1;
	}
	
	public MainListAdapter getAdapter(){
		return mla;
	}
	
	public void setupAdapter() {
		lv = (ListView) findViewById(R.id.activity_main_question_list);
		mla = new MainListAdapter(this, R.layout.activity_main_question_entity,
				pc.getQuestionsInstance());
		lv.setAdapter(mla);
	}

	public void toQuestionActivity(int position) {
		Intent i = new Intent( this, ViewQuestion.class );
		i.putExtra("question_id", pc.getQuestionsInstance().get(position).getId());
		pc.addReadQuestion(pc.getQuestionsInstance().get(position));
		startActivity(i);
	}

}

