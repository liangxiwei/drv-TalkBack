package com.example.jrd48.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.ShowPhotoActivity;
import com.example.jrd48.chat.FileTransfer.TransferService;
import com.example.jrd48.chat.FileTransfer.VideoActivity;
import com.example.jrd48.chat.PicTool.BitmapPercent;
import com.luobin.dvr.R;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MsgAdapter extends ArrayAdapter<Msg>{

	private int resourceId;
	private List<Msg> list;
	private String myPhone;
	private List<MsgPicInfo> msgPicInfos = new ArrayList<>();

	public MsgAdapter(Context context, int textViewResourceId, List<Msg> objects, String myPhone) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		list = objects;
		resourceId = textViewResourceId;
		SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
		this.myPhone = myPhone;
	}

	@Override
	public boolean isEnabled(int position) {
		if (position >= list.size()) {
			return false;
		}
		// TODO Auto-generated method stub
		if (list.get(position).getType() == Msg.TYPE_MSG_RECORD
				|| list.get(position).getType() == Msg.TYPE_MSG_CANCEL
				|| list.get(position).getType() == Msg.TYPE_MSG_MY_CANCEL)
			return false;// 表示不能点击
		return super.isEnabled(position);
	}

	public View getView(int position,View convertView, ViewGroup parent){
		View view;
		final Msg msg = getItem(position);
		final ViewHolder viewHolder;
		if (convertView == null){
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
			viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
			viewHolder.centerLayout = (LinearLayout) view.findViewById(R.id.center_layout);
			viewHolder.rightMsg = (TextView) view.findViewById(R.id.right_msg);
			viewHolder.leftMsg = (TextView) view.findViewById(R.id.left_msg);
			viewHolder.leftImage = (ImageView) view.findViewById(R.id.left_image);
			viewHolder.rightImage = (ImageView) view.findViewById(R.id.right_image);
			viewHolder.rightTime = (TextView) view.findViewById(R.id.right_time);
			viewHolder.leftTime = (TextView) view.findViewById(R.id.left_time);
			viewHolder.msg = (TextView) view.findViewById(R.id.msg);
			viewHolder.myhead = (ImageView) view.findViewById(R.id.circle_r);
			viewHolder.otherhead = (ImageView) view.findViewById(R.id.circle_l);
			viewHolder.leftsending = (ImageView) view.findViewById(R.id.left_sending);
			viewHolder.rightsending = (ImageView) view.findViewById(R.id.right_sending);
			viewHolder.leftPlayer = (ImageView) view.findViewById(R.id.left_player);
			viewHolder.rightPlayer = (ImageView) view.findViewById(R.id.right_player);
			view.setTag(viewHolder);
		}
		else{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.leftPlayer.setVisibility(View.GONE);
		viewHolder.rightPlayer.setVisibility(View.GONE);
		if (msg.getType() == Msg.TYPE_MSG_RECORD) {
			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.centerLayout.setVisibility(View.VISIBLE);
			viewHolder.msg.setText(msg.getContent() + "—" + msg.getTime());
		} else if (msg.getType() == Msg.TYPE_MSG_CANCEL) {
			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.centerLayout.setVisibility(View.VISIBLE);
			viewHolder.msg.setText(R.string.other_msg_cancel);
		} else if (msg.getType() == Msg.TYPE_MSG_MY_CANCEL) {
			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.centerLayout.setVisibility(View.VISIBLE);
			viewHolder.msg.setText(R.string.my_msg_cancel);
		} else if (msg.getType() == Msg.TYPE_RECEIVED) {
			viewHolder.centerLayout.setVisibility(View.GONE);
			viewHolder.leftsending.setVisibility(View.GONE);

			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.leftMsg.setText(msg.getContent());
			viewHolder.leftMsg.setVisibility(View.VISIBLE);
			viewHolder.leftImage.setVisibility(View.GONE);
			viewHolder.leftTime.setText(msg.getTime());
			Bitmap bitmap = GlobalImg.getImage(getContext(), msg.getPhone());
			if (bitmap == null) {
				viewHolder.otherhead.setImageResource(R.drawable.default_useravatar);
			} else {
				viewHolder.otherhead.setImageBitmap(bitmap);
			}
			viewHolder.otherhead.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {
					//((FirstActivity) getContext()).getList().performLongClick();
					((FirstActivity) getContext()).editAddText(msg.getPhone());
					return true;
				}
			});
		}
		else if(msg.getType() == Msg.TYPE_RECEIVED_IMAGE){
			viewHolder.centerLayout.setVisibility(View.GONE);
			viewHolder.leftsending.setVisibility(View.GONE);

			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.leftImage.setImageBitmap(msg.getBitmap());
			viewHolder.leftMsg.setVisibility(View.GONE);
			viewHolder.leftImage.setVisibility(View.VISIBLE);
			setMsgImageOnclicked(viewHolder.leftImage, msg.getMsgID());
			Bitmap bitmap = GlobalImg.getImage(getContext(), msg.getPhone());
			if (bitmap == null) {
				viewHolder.otherhead.setImageResource(R.drawable.default_useravatar);
			} else {
				viewHolder.otherhead.setImageBitmap(bitmap);
			}

			viewHolder.leftTime.setText(msg.getTime());
			viewHolder.otherhead.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {
					//((FirstActivity) getContext()).getList().performLongClick();
					((FirstActivity) getContext()).editAddText(msg.getPhone());
					return true;
				}
			});
		}
		else if(msg.getType() == Msg.TYPE_SENT){
			viewHolder.centerLayout.setVisibility(View.GONE);
			if (msg.getSentState() == Msg.SENTS) {
				viewHolder.rightsending.setVisibility(View.GONE);
			} else if (msg.getSentState() == Msg.SENTF) {
				viewHolder.rightsending.setVisibility(View.VISIBLE);
				viewHolder.rightsending.setBackgroundResource(R.drawable.re_sent);
				final String content = msg.getContent();
				viewHolder.rightsending.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						((FirstActivity) getContext()).DeleteMsg(msg.getSn());
						((FirstActivity) getContext()).saveT(content, Msg.TYPE_SENT);
					}
				});
			} else if (msg.getSentState() == Msg.SENDING) {
				viewHolder.rightsending.setVisibility(View.VISIBLE);
				viewHolder.rightsending.setBackgroundResource(R.drawable.loading);
				AnimationDrawable rightAniDraw = (AnimationDrawable) viewHolder.rightsending.getBackground();
				rightAniDraw.start();
			}

			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightMsg.setText(msg.getContent());
			viewHolder.rightMsg.setVisibility(View.VISIBLE);
			viewHolder.rightImage.setVisibility(View.GONE);
			viewHolder.rightTime.setText(msg.getTime());
			viewHolder.myhead.setImageBitmap(GlobalImg.getImage(getContext(), myPhone));
		}
		else if(msg.getType() == Msg.TYPE_SENT_IMAGE){
			viewHolder.centerLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLayout.setVisibility(View.GONE);
			int percent = msg.getPercent();
			//Log.i("jrdchat", "传入的百分比：" + percent);
			if (percent >= 0 && percent <= 100) {
				if (msg.getSentState() == Msg.SENTF) {
					viewHolder.rightsending.setVisibility(View.VISIBLE);
					viewHolder.rightsending.setBackgroundResource(R.drawable.re_sent);
					viewHolder.rightsending.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							((FirstActivity) getContext()).DeleteMsg(msg.getSn());
							((FirstActivity) getContext()).saveI(msg.getBitmap(), Msg.TYPE_SENT_IMAGE, msg.getAddress());
						}
					});
				} else {
					Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, percent);
					viewHolder.rightImage.setImageBitmap(bitmap);
					viewHolder.rightsending.setVisibility(View.VISIBLE);
					viewHolder.rightsending.setBackgroundResource(R.drawable.loading);
					AnimationDrawable rightAniDraw = (AnimationDrawable) viewHolder.rightsending.getBackground();
					rightAniDraw.start();
				}
			} else if (percent == -1) {
				Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, 0);
				viewHolder.rightImage.setImageBitmap(bitmap);
				viewHolder.rightsending.setVisibility(View.VISIBLE);
				viewHolder.rightsending.setBackgroundResource(R.drawable.re_sent);
				viewHolder.rightsending.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (msg.getMsgID() != 0) {
							String fromPhone;
							if (msg.getTeamID() == 0) {
								fromPhone = ((FirstActivity) getContext()).getLinkmanPhone();
							} else {
								fromPhone = msg.getPhone();
							}
							Intent i = new Intent(getContext(), TransferService.class);
							i.putExtra("type",TransferService.UPLOAD_FILE);
							i.putExtra("teamid",msg.getTeamID());
							i.putExtra("phone",fromPhone);
							i.putExtra("msgid",msg.getMsgID());
							i.putExtra("address",msg.getAddress());
							getContext().startService(i);
							/*((FirstActivity) getContext()).getmUploadFile().startUpload(new TansferFileUp(
									msg.getTeamID(), fromPhone, msg.getMsgID(), msg.getAddress()));*/
						} else {
							((FirstActivity) getContext()).DeleteMsg(msg.getSn());
							((FirstActivity) getContext()).saveI(msg.getBitmap(), Msg.TYPE_SENT_IMAGE, msg.getAddress());
						}
					}
				});
			} else if (percent == 101) {
				Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, 100);
				viewHolder.rightImage.setImageBitmap(bitmap);
				viewHolder.rightsending.setVisibility(View.GONE);
			} else {
				Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, 0);
				viewHolder.rightImage.setImageBitmap(bitmap);
				viewHolder.rightsending.setVisibility(View.GONE);
			}
			//viewHolder.rightImage.setImageBitmap(msg.getBitmap());
			viewHolder.rightMsg.setVisibility(View.GONE);
			viewHolder.rightImage.setVisibility(View.VISIBLE);
			setMsgImageOnclicked(viewHolder.rightImage, msg.getMsgID());
			viewHolder.rightTime.setText(msg.getTime());
			viewHolder.myhead.setImageBitmap(GlobalImg.getImage(getContext(), myPhone));
		} else if (msg.getType() == Msg.TYPE_RECEIVED_VIDEO) {
			viewHolder.leftPlayer.setVisibility(View.VISIBLE);
			viewHolder.centerLayout.setVisibility(View.GONE);
			viewHolder.leftsending.setVisibility(View.GONE);

			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.leftImage.setImageBitmap(msg.getBitmap());
			viewHolder.leftMsg.setVisibility(View.GONE);
			viewHolder.leftImage.setVisibility(View.VISIBLE);
			setMsgImageOnclicked(viewHolder.leftImage, msg.getMsgID(), msg.getPhone(), msg.getTeamID(), msg.getAddress(), msg.getSn(), msg.getBitmap());
			Bitmap bitmap = GlobalImg.getImage(getContext(), msg.getPhone());
			if (bitmap == null) {
				viewHolder.otherhead.setImageResource(R.drawable.default_useravatar);
			} else {
				viewHolder.otherhead.setImageBitmap(bitmap);
			}

			viewHolder.leftTime.setText(msg.getTime());
			viewHolder.otherhead.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {
					//((FirstActivity) getContext()).getList().performLongClick();
					((FirstActivity) getContext()).editAddText(msg.getPhone());
					return true;
				}
			});
		} else if (msg.getType() == Msg.TYPE_SENT_VIDEO) {
			viewHolder.rightPlayer.setVisibility(View.VISIBLE);
			viewHolder.centerLayout.setVisibility(View.GONE);

			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLayout.setVisibility(View.GONE);
			int percent = msg.getPercent();
			//Log.i("jrdchat", "传入的百分比：" + percent);
			if (percent >= 0 && percent <= 100) {
				if (msg.getSentState() == Msg.SENTF) {
					viewHolder.rightsending.setVisibility(View.VISIBLE);
					viewHolder.rightsending.setBackgroundResource(R.drawable.re_sent);
					viewHolder.rightsending.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							((FirstActivity) getContext()).DeleteMsg(msg.getSn());
							((FirstActivity) getContext()).saveI(msg.getBitmap(), Msg.TYPE_SENT_VIDEO, msg.getAddress());
						}
					});

				} else {
					Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, percent);
					viewHolder.rightImage.setImageBitmap(bitmap);
					viewHolder.rightsending.setVisibility(View.VISIBLE);
					viewHolder.rightsending.setBackgroundResource(R.drawable.loading);
					AnimationDrawable rightAniDraw = (AnimationDrawable) viewHolder.rightsending.getBackground();
					rightAniDraw.start();
				}
			} else if (percent == -1) {
				Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, 0);
				viewHolder.rightImage.setImageBitmap(bitmap);
				viewHolder.rightsending.setVisibility(View.VISIBLE);
				viewHolder.rightsending.setBackgroundResource(R.drawable.re_sent);
				viewHolder.rightsending.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (msg.getMsgID() != 0) {
							String fromPhone;
							if (msg.getTeamID() == 0) {
								fromPhone = ((FirstActivity) getContext()).getLinkmanPhone();
							} else {
								fromPhone = msg.getPhone();
							}
							Intent i = new Intent(getContext(), TransferService.class);
							i.putExtra("type",TransferService.UPLOAD_FILE);
							i.putExtra("teamid",msg.getTeamID());
							i.putExtra("phone",fromPhone);
							i.putExtra("msgid",msg.getMsgID());
							i.putExtra("address",msg.getAddress());
							getContext().startService(i);
							/*((FirstActivity) getContext()).getmUploadFile().startUpload(new TansferFileUp(
									msg.getTeamID(), fromPhone, msg.getMsgID(), msg.getAddress()));*/
						} else {
							((FirstActivity) getContext()).DeleteMsg(msg.getSn());
							((FirstActivity) getContext()).saveI(msg.getBitmap(), Msg.TYPE_SENT_VIDEO, msg.getAddress());
						}

					}
				});
			} else if (percent == 101) {
				Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, 100);
				viewHolder.rightImage.setImageBitmap(bitmap);
				viewHolder.rightsending.setVisibility(View.GONE);
			} else {
				Bitmap bitmap = BitmapPercent.setAlphaPercent(msg.getBitmap(), 50, 0);
				viewHolder.rightImage.setImageBitmap(bitmap);
				viewHolder.rightsending.setVisibility(View.GONE);
			}
			//viewHolder.rightImage.setImageBitmap(msg.getBitmap());
			viewHolder.rightMsg.setVisibility(View.GONE);
			viewHolder.rightImage.setVisibility(View.VISIBLE);
			setMsgImageOnclicked(viewHolder.rightImage, msg.getMsgID(), msg.getPhone(), msg.getTeamID(), msg.getAddress(), msg.getSn(), msg.getBitmap());
			viewHolder.rightTime.setText(msg.getTime());
			viewHolder.myhead.setImageBitmap(GlobalImg.getImage(getContext(), myPhone));
		}

		viewHolder.otherhead.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String phone = msg.getPhone();
				if (phone != null && !phone.equals("")) {
					((FirstActivity) getContext()).showsinfo(msg.getPhone());
				}
			}
		});
		return view;
	}

	byte[] byteMap;

	private void setMsgImageOnclicked(ImageView imageView1, long _msgID, String _phone, long _teamID, String _address, long _sn, Bitmap _bitmap) {

		final long msgId = _msgID;
		final long teamId = _teamID;
		final long sn = _sn;
		final String otherphone = _phone;
		final String address = _address;
		final Bitmap bitmap = _bitmap;

		imageView1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((FirstActivity) getContext()).photo_or_no = true;
				if (bitmap != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
					byteMap = baos.toByteArray();
				}
				Intent i = new Intent(view.getContext(), VideoActivity.class);
				i.putExtra("msgid", msgId);
				i.putExtra("teamid", teamId);
				i.putExtra("sn", sn);
				i.putExtra("otherphone", otherphone);
				i.putExtra("address", address);
				i.putExtra("bitmap", byteMap);
				view.getContext().startActivity(i);
			}
		});
		imageView1.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				//((FirstActivity) getContext()).getList().performLongClick();
				return false;
			}
		});
	}

	private void setMsgImageOnclicked(ImageView imageView1, long _msgID) {

		final long msgId = _msgID;
		imageView1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((FirstActivity) getContext()).photo_or_no = true;
				msgPicInfos.clear();
				int currentNum = 0;
				for (Msg msg : list) {
					if (msg.getType() == Msg.TYPE_SENT_IMAGE ||
							msg.getType() == Msg.TYPE_RECEIVED_IMAGE) {
						MsgPicInfo mp = new MsgPicInfo(msg);
						if (msg.getBitmap() != null) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							msg.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
							mp.setPictures(baos.toByteArray());
						}
						msgPicInfos.add(mp);
						if (msg.getMsgID() == msgId) {
							currentNum = msgPicInfos.size() - 1;
						}
					}
				}
				Intent i = new Intent(view.getContext(), ShowPhotoActivity.class);
				i.putExtra("curr_index", currentNum);
				i.putExtra("all_pic_info", (Serializable) msgPicInfos);
//				i.putExtra("msgid", msgId);
//				i.putExtra("teamid", teamId);
//				i.putExtra("sn", sn);
//				i.putExtra("otherphone", otherphone);
//				i.putExtra("address", address);
				view.getContext().startActivity(i);
			}
		});
        imageView1.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                //((FirstActivity) getContext()).getList().performLongClick();
                return false;
            }
        });
    }

	class ViewHolder{
		LinearLayout leftLayout;
		LinearLayout rightLayout;
		LinearLayout centerLayout;
		TextView leftMsg;
		TextView rightMsg;
		ImageView leftImage;
		ImageView rightImage;
		TextView leftTime;
		TextView rightTime;
		TextView msg;
		ImageView leftsending;
		ImageView rightsending;
		ImageView myhead;
		ImageView otherhead;
		ImageView leftPlayer;
		ImageView rightPlayer;
	}

}
