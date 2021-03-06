package net.ess3.commands;

import java.util.List;
import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import net.ess3.utils.FormatUtil;
import net.ess3.utils.Util;
import org.bukkit.command.CommandSender;


public class Commandmail extends EssentialsCommand
{
	//TODO: Tidy this up
	@Override
	public void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length >= 1 && "read".equalsIgnoreCase(args[0]))
		{
			final List<String> mail = user.getData().getMails();
			if (mail == null || mail.isEmpty())
			{
				user.sendMessage(_("You do not have any mail."));
				throw new NoChargeException();
			}
			for (String messages : mail)
			{
				user.sendMessage(messages);
			}
			user.sendMessage(_("To mark your mail as read, type /mail clear."));
			return;
		}
		if (args.length >= 3 && "send".equalsIgnoreCase(args[0]))
		{
			if (!Permissions.MAIL_SEND.isAuthorized(user))
			{
				throw new Exception(_("You do not have the {0} permission.", "essentials.mail.send"));
			}

			IUser u = ess.getUserMap().matchUser(args[1], true);
			if (u == null)
			{
				throw new Exception(_("Player {0} was never on this server.", args[1]));
			}
			if (!u.isIgnoringPlayer(user))
			{
				final String mail = Util.sanitizeString(FormatUtil.stripFormat(getFinalArg(args, 2)));
				u.addMail(user.getName() + ": " + mail);
			}
			user.sendMessage(_("Mail sent!"));
			return;
		}
		if (args.length > 1 && "sendall".equalsIgnoreCase(args[0]))
		{
			if (!Permissions.MAIL_SENDALL.isAuthorized(user))
			{
				throw new Exception(_("You do not have the {0} permission.", "essentials.mail.sendall"));
			}
			ess.getPlugin().runTaskAsynchronously(new SendAll(user.getName() + ": " + FormatUtil.stripColor(getFinalArg(args, 1))));
			user.sendMessage(_("Mail sent!"));
			return;
		}
		if (args.length >= 1 && "clear".equalsIgnoreCase(args[0]))
		{
			user.getData().clearMails();
			user.queueSave();
			user.sendMessage(_("Mail Cleared!"));
			return;
		}
		throw new NotEnoughArgumentsException();
	}

	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length >= 1 && "read".equalsIgnoreCase(args[0]))
		{
			throw new Exception(_("Only in-game players can use {0}.", commandName + " read"));
		}
		else if (args.length >= 1 && "clear".equalsIgnoreCase(args[0]))
		{
			throw new Exception(_("Only in-game players can use {0}.", commandName + " clear"));
		}
		else if (args.length >= 3 && "send".equalsIgnoreCase(args[0]))
		{
			IUser u = ess.getUserMap().matchUser(args[1], true);
			u.addMail("Server: " + getFinalArg(args, 2));
			sender.sendMessage(_("Mail sent!"));
			return;
		}
		else if (args.length >= 1 && "sendall".equalsIgnoreCase(args[0]))
		{
			ess.getPlugin().runTaskAsynchronously(new SendAll("Server: " + getFinalArg(args, 2)));
		}
		else if (args.length >= 2)
		{
			//allow sending from console without "send" argument, since it's the only thing the console can do
			IUser u = ess.getUserMap().matchUser(args[0], true);
			u.addMail("Server: " + getFinalArg(args, 1));
			sender.sendMessage(_("Mail sent!"));
			return;
		}
		throw new NotEnoughArgumentsException();
	}


	private class SendAll implements Runnable
	{
		String message;

		public SendAll(String message)
		{
			this.message = message;
		}

		@Override
		public void run()
		{
			for (String username : ess.getUserMap().getAllUniqueUsers())
			{
				final IUser user = ess.getUserMap().getUser(username);
				if (user != null)
				{
					user.addMail(message);
				}
			}
		}
	}
}
