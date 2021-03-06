package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.permissions.Permissions;
import net.ess3.user.PlayerNotFoundException;
import net.ess3.user.UserData.TimestampType;
import net.ess3.utils.DateUtil;
import org.bukkit.command.CommandSender;


public class Commandseen extends EssentialsCommand
{
	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		seen(sender, args, true);
	}

	@Override
	protected void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		seen(user, args, Permissions.SEEN_BANREASON.isAuthorized(user));
	}

	protected void seen(final CommandSender sender, final String[] args, final boolean show) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}
		try
		{
			final IUser u = ess.getUserMap().matchUserExcludingHidden(args[0], getPlayerOrNull(sender));
			u.setDisplayNick();
			sender.sendMessage(_("Player {0} is online since {1}.", u.getPlayer().getDisplayName(), DateUtil.formatDateDiff(u.getTimestamp(TimestampType.LOGIN))));
			if(u.getData().isAfk())
			{
				sender.sendMessage(_(" - AFK: {0}", _("true")));
			}
			if(u.getData().isJailed())
			{
				sender.sendMessage(_(" - Jail: {0}", u.getTimestamp(TimestampType.JAIL) > 0
								   ? DateUtil.formatDateDiff(u.getTimestamp(TimestampType.JAIL))
								   : _("true")));
			}
			if(u.getData().isMuted())
			{
				sender.sendMessage(_(" - Muted: {0}", u.getTimestamp(TimestampType.MUTE) > 0
								   ? DateUtil.formatDateDiff(u.getTimestamp(TimestampType.MUTE))
								   : _("true")));
			}
		}
		catch (PlayerNotFoundException e)
		{
			final IUser u = ess.getUserMap().matchUser(args[0], true);
			sender.sendMessage(_("Player {0} is offline since {1}.", u.getName(), DateUtil.formatDateDiff(u.getTimestamp(TimestampType.LOGOUT))));
			if (u.isBanned())
			{
				sender.sendMessage(_(" - Banned: {0}", show ? u.getData().getBan().getReason() : _("true")));
			}
		}
	}
}
