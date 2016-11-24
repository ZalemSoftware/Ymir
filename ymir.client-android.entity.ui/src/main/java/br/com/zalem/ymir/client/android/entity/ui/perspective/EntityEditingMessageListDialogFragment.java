package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.ui.R;

/**
 * Diálogo de exibição da lista de mensagens de edição utilizado pelo {@link EntityEditingPerspective}.<br>
 * As mensagens devem ser agrupadas em instâncias de {@link MessageGroup} e passadas através do argumento {@link #MESSAGE_GROUPS_ARGUMENT}.
 * O título do diálogo pode ser definido com o id para umr recurso String através do argumento {@link #DIALOG_TITLE_ARGUMENT}.<br>
 * Nenhum dos dois argumentos é obrigatório, sendo que se não houver grupos de mensagens, um texto informativa será
 * exibido no lugar ({@link R.string#entity_editing_message_list_dialog_message_empty}).
 * 
 * @author Thiago Gesser
 */
public final class EntityEditingMessageListDialogFragment extends DialogFragment {

	/**
	 * {@link ArrayList} contendo os {@link MessageGroup} que serão exibidos.
	 */
	public static final String MESSAGE_GROUPS_ARGUMENT = "MESSAGE_GROUPS_ARGUMENT";
	
	/**
	 * Id para um recurso String que será utilizado como o título.
	 */
	public static final String DIALOG_TITLE_ARGUMENT = "DIALOG_TITLE_ARGUMENT";

    @Override
    @NonNull
    @SuppressLint("InflateParams")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Obtém os argumentos (opcionais)
		int titleId = 0;
		ArrayList<MessageGroup> messageGroups = null;

		Bundle arguments = getArguments();
		if (arguments != null) {
			titleId = arguments.getInt(DIALOG_TITLE_ARGUMENT);
			messageGroups = arguments.getParcelableArrayList(MESSAGE_GROUPS_ARGUMENT);
		}

		//Cria a View customizada do dialog.
		Context context = getActivity();
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.entity_editing_message_list_dialog, null);
		TextView messageView = (TextView) view.findViewById(R.id.entity_editing_message_list_dialog_text);

		if (messageGroups == null || messageGroups.isEmpty()) {
			messageView.setText(R.string.entity_editing_message_list_dialog_message_empty);
		} else {
			messageView.setText(R.string.entity_editing_message_list_dialog_message);

			//Só define o adapter se houver mensagens.
			ListView listView = (ListView) view.findViewById(R.id.entity_editing_message_list_dialog_list);
			listView.setAdapter(new MessageAdapter(context, messageGroups));
		}

		Builder builder = new Builder(context);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setView(view);

		//Define o título do dialog, se houver.
		if (titleId != 0) {
			builder.setTitle(titleId);
		}

        AlertDialog dialog = builder.create();
        AndroidBugsUtils.applyWorkaroundForAlertDialogWithFlexibleListViewBug(dialog, view);
        return dialog;
	}


    /*
	 * Classes auxiliares
	 */

    public enum MessageType {ERROR, WARNING}

    /**
     * Representa uma mensagem de edição de registro.
     */
    public static final class Message implements Parcelable {
        private final String text;
        private final MessageType type;
        private final String tag;

        public Message(String text, MessageType type, String tag) {
            this.text = text;
            this.type = type;
            this.tag = tag;
        }

        public String getText() {
            return text;
        }

        public MessageType getType() {
            return type;
        }

        public String getTag() {
            return tag;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeInt(type.ordinal());
            dest.writeString(tag);
        }

        public static final Creator<Message> CREATOR = new Creator<Message>() {

            @Override
            public Message createFromParcel(Parcel source) {
                return new Message(source.readString(), MessageType.values()[source.readInt()], source.readString());
            }

            @Override
            public Message[] newArray(int size) {
                return new Message[size];
            }
        };
    }

	/**
	 * Representa um agrupamento de {@link Message}. É possível definir um nome e um detalhamento para o grupo, que
	 * serão exibidos no diálogo.
	 */
	public static final class MessageGroup implements Parcelable {
		private final String name;
		private final String detail;
		private final List<Message> messages;

		public MessageGroup(String name, String detail, List<Message> messages) {
			this.name = name;
			this.detail = detail;
			this.messages = messages;
		}
		
		public String getName() {
			return name;
		}
		
		public String getDetail() {
			return detail;
		}
		
		public List<Message> getMessages() {
			return messages;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(name);
			dest.writeString(detail);
			dest.writeList(messages);
		}
		
		public static final Creator<MessageGroup> CREATOR = new Creator<MessageGroup>() {
			@Override
			public MessageGroup createFromParcel(Parcel source) {
				String name = source.readString();
				String detail = source.readString();
				ArrayList<Message> messages = AndroidBugsUtils.applyWorkaroundForArrayListDefaultClassloaderBug(source);
				return new MessageGroup(name, detail, messages);
			}
			
			@Override
			public MessageGroup[] newArray(int size) {
				return new MessageGroup[size];
			}
		};
	}
	
	/**
	 * Adaptador de grupos e suas mensagens. Utiliza um tipo de View específica para grupos e outro para mensagens.
	 */
	private static final class MessageAdapter extends BaseAdapter {

		private static final int GROUP_HEADER_VIEW_TYPE = 0;
		private static final int MESSAGE_VIEW_TYPE = 1;
		
		private final List<MessageGroup> groups;
		private final LayoutInflater inflater;

		public MessageAdapter(Context context, List<MessageGroup> groups) {
			if (groups == null || groups.isEmpty()) {
				throw new IllegalArgumentException("groups == null || groups.length == 0");
			}
			this.groups = groups;
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			//Cada grupo terá um header, então cada um deles conta como um item na contagem, somado as suas mensagens.
			int count = groups.size();
			for (MessageGroup group : groups) { 
				count += group.getMessages().size();
			}
			return count;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			//GROUP_HEADER_VIEW_TYPE e MESSAGE_VIEW_TYPE.
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			int curPosition = position;
			for (MessageGroup group : groups) {
				if (curPosition-- == 0) {
					return GROUP_HEADER_VIEW_TYPE;
				}
				
				int msgsSize = group.getMessages().size();
				if (curPosition < msgsSize) {
					return MESSAGE_VIEW_TYPE;
				}
				curPosition -= msgsSize; 
			}
			throw new IllegalArgumentException("Invalid position: " + position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int curPosition = position;
			for (MessageGroup group : groups) {
				if (curPosition-- == 0) {
					return getGroupHeaderView(group, convertView, parent);
				}

				List<Message> messages = group.getMessages();
				int msgsSize = messages.size();
				if (curPosition < msgsSize) {
					return getMessageView(messages.get(curPosition), convertView, parent);
				}
				curPosition -= msgsSize;
			}
			throw new IllegalArgumentException("Invalid position: " + position);
		}

		@Override
		public MessageGroup getItem(int position) {
			//Não utiliza este método, mas é obrigado a implementá-lo, então apenas retorna o grupo associado à posição.
			int curPosition = position;
			for (MessageGroup group : groups) {
				int msgsSize = group.getMessages().size();
				if (curPosition <= msgsSize) {
					return group;
				}
				curPosition -= msgsSize; 
			}
			throw new IllegalArgumentException("Invalid position: " + position);
		}
		
		
		/*
		 * Métodos auxiliares
		 */
		
		private View getGroupHeaderView(MessageGroup group, View convertView, ViewGroup parent) {
			GroupHeaderViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.entity_editing_message_list_header_item, parent, false);
				TextView nameView = (TextView) convertView.findViewById(R.id.entity_editing_message_list_header_item_name);
				TextView detailView = (TextView) convertView.findViewById(R.id.entity_editing_message_list_header_item_detail);
				viewHolder = new GroupHeaderViewHolder(nameView, detailView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (GroupHeaderViewHolder) convertView.getTag();
			}
			
			//Seta o nome do grupo
			viewHolder.getNameView().setText(group.getName());
			
			//Seta o detalhe do grupo, se houver. 
			String detail = group.getDetail();
			viewHolder.getDetailView().setText(detail);

			return convertView;
		}
		
		private View getMessageView(Message message, View convertView, ViewGroup parent) {
			MessageViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.entity_editing_message_list_message_item, parent, false);
				ImageView iconView = (ImageView) convertView.findViewById(R.id.entity_editing_message_list_message_item_icon);
				TextView textView = (TextView) convertView.findViewById(R.id.entity_editing_message_list_message_item_text);
				viewHolder = new MessageViewHolder(iconView, textView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (MessageViewHolder) convertView.getTag();
			}
			
			//Seta o ícone de acordo com o tipo da mensagem.
			switch (message.getType()) {
				case WARNING:
					viewHolder.getIconView().setImageResource(R.drawable.ic_msg_warning);
					break;
				case ERROR:
					viewHolder.getIconView().setImageResource(R.drawable.ic_msg_error);
					break;
					
				default:
					throw new IllegalArgumentException("Unsupported MessageType: " + message.getType());
			}
			
			//Seta o texto da mensagem.
			viewHolder.getTextView().setText(message.getText());
			return convertView;
		}
		
		
		/*
		 * Classes auxiliares
		 */
		
		private static final class GroupHeaderViewHolder { 
			private final TextView nameView;
			private final TextView detailView;
			
			public GroupHeaderViewHolder(TextView nameView, TextView detailView) {
				this.nameView = nameView;
				this.detailView = detailView;
			}
			
			public TextView getDetailView() {
				return detailView;
			}
			
			public TextView getNameView() {
				return nameView;
			}
		}
		
		private static final class MessageViewHolder { 
			private final ImageView iconView;
			private final TextView textView;
			
			public MessageViewHolder(ImageView iconView, TextView textView) {
				this.iconView = iconView;
				this.textView = textView;
			}
			
			public ImageView getIconView() {
				return iconView;
			}
			
			public TextView getTextView() {
				return textView;
			}
		}
	}
}
