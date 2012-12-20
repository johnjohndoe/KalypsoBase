/*
 * @(#)smtpsend.java	1.5 05/12/09
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

import java.io.*;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.smtp.*;

/**
 * Demo app that shows how to construct and send an RFC822
 * (singlepart) message.
 *
 * XXX - allow more than one recipient on the command line
 *
 * This is just a variant of msgsend.java that demonstrates use of
 * some SMTP-specific features.
 *
 * @author Max Spivak
 * @author Bill Shannon
 */

public class smtpsend {

    public static void main(String[] argv) {
	String  to, subject = null, from = null, 
		cc = null, bcc = null, url = null;
	String mailhost = null;
	String mailer = "smtpsend";
	String file = null;
	String protocol = null, host = null, user = null, password = null;
	String record = null;	// name of folder in which to record mail
	boolean debug = false;
	boolean verbose = false;
	boolean auth = false;
	boolean ssl = false;
	BufferedReader in =
			new BufferedReader(new InputStreamReader(System.in));
	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-T")) {
		protocol = argv[++optind];
	    } else if (argv[optind].equals("-H")) {
		host = argv[++optind];
	    } else if (argv[optind].equals("-U")) {
		user = argv[++optind];
	    } else if (argv[optind].equals("-P")) {
		password = argv[++optind];
	    } else if (argv[optind].equals("-M")) {
		mailhost = argv[++optind];
	    } else if (argv[optind].equals("-f")) {
		record = argv[++optind];
	    } else if (argv[optind].equals("-a")) {
		file = argv[++optind];
	    } else if (argv[optind].equals("-s")) {
		subject = argv[++optind];
	    } else if (argv[optind].equals("-o")) { // originator
		from = argv[++optind];
	    } else if (argv[optind].equals("-c")) {
		cc = argv[++optind];
	    } else if (argv[optind].equals("-b")) {
		bcc = argv[++optind];
	    } else if (argv[optind].equals("-L")) {
		url = argv[++optind];
	    } else if (argv[optind].equals("-d")) {
		debug = true;
	    } else if (argv[optind].equals("-v")) {
		verbose = true;
	    } else if (argv[optind].equals("-A")) {
		auth = true;
	    } else if (argv[optind].equals("-S")) {
		ssl = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: smtpsend [[-L store-url] | [-T prot] [-H host] [-U user] [-P passwd]]");
		System.out.println(
"\t[-s subject] [-o from-address] [-c cc-addresses] [-b bcc-addresses]");
		System.out.println(
"\t[-f record-mailbox] [-M transport-host] [-d] [-a attach-file]");
		System.out.println(
"\t[-v] [-A] [-S] [address]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	try {
	    if (optind < argv.length) {
		// XXX - concatenate all remaining arguments
		to = argv[optind];
		System.out.println("To: " + to);
	    } else {
		System.out.print("To: ");
		System.out.flush();
		to = in.readLine();
	    }
	    if (subject == null) {
		System.out.print("Subject: ");
		System.out.flush();
		subject = in.readLine();
	    } else {
		System.out.println("Subject: " + subject);
	    }

	    Properties props = System.getProperties();
	    if (mailhost != null)
		props.put("mail.smtp.host", mailhost);
	    if (auth)
		props.put("mail.smtp.auth", "true");

	    // Get a Session object
	    Session session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    // construct the message
	    Message msg = new MimeMessage(session);
	    if (from != null)
		msg.setFrom(new InternetAddress(from));
	    else
		msg.setFrom();

	    msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
	    if (cc != null)
		msg.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(cc, false));
	    if (bcc != null)
		msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(bcc, false));

	    msg.setSubject(subject);

	    String text = collect(in);

	    if (file != null) {
		// Attach the specified file.
		// We need a multipart message to hold the attachment.
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(text);
		MimeBodyPart mbp2 = new MimeBodyPart();
		mbp2.attachFile(file);
		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
		mp.addBodyPart(mbp2);
		msg.setContent(mp);
	    } else {
		// If the desired charset is known, you can use
		// setText(text, charset)
		msg.setText(text);
	    }

	    msg.setHeader("X-Mailer", mailer);
	    msg.setSentDate(new Date());

	    // send the thing off
	    /*
	     * The simple way to send a message is this:
	     *
	    Transport.send(msg);
	     *
	     * But we're going to use some SMTP-specific features for
	     * demonstration purposes so we need to manage the Transport
	     * object explicitly.
	     */
	    SMTPTransport t =
		(SMTPTransport)session.getTransport(ssl ? "smtps" : "smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
		t.sendMessage(msg, msg.getAllRecipients());
	    } finally {
		if (verbose)
		    System.out.println("Response: " +
						t.getLastServerResponse());
		t.close();
	    }

	    System.out.println("\nMail was sent successfully.");

	    // Keep a copy, if requested.

	    if (record != null) {
		// Get a Store object
		Store store = null;
		if (url != null) {
		    URLName urln = new URLName(url);
		    store = session.getStore(urln);
		    store.connect();
		} else {
		    if (protocol != null)		
			store = session.getStore(protocol);
		    else
			store = session.getStore();

		    // Connect
		    if (host != null || user != null || password != null)
			store.connect(host, user, password);
		    else
			store.connect();
		}

		// Get record Folder.  Create if it does not exist.
		Folder folder = store.getFolder(record);
		if (folder == null) {
		    System.err.println("Can't get record folder.");
		    System.exit(1);
		}
		if (!folder.exists())
		    folder.create(Folder.HOLDS_MESSAGES);

		Message[] msgs = new Message[1];
		msgs[0] = msg;
		folder.appendMessages(msgs);

		System.out.println("Mail was recorded successfully.");
	    }

	} catch (Exception e) {
	    if (e instanceof SendFailedException) {
		MessagingException sfe = (MessagingException)e;
		if (sfe instanceof SMTPSendFailedException) {
		    SMTPSendFailedException ssfe =
				    (SMTPSendFailedException)sfe;
		    System.out.println("SMTP SEND FAILED:");
		    if (verbose)
			System.out.println(ssfe.toString());
		    System.out.println("  Command: " + ssfe.getCommand());
		    System.out.println("  RetCode: " + ssfe.getReturnCode());
		    System.out.println("  Response: " + ssfe.getMessage());
		} else {
		    if (verbose)
			System.out.println("Send failed: " + sfe.toString());
		}
		Exception ne;
		while ((ne = sfe.getNextException()) != null &&
			ne instanceof MessagingException) {
		    sfe = (MessagingException)ne;
		    if (sfe instanceof SMTPAddressFailedException) {
			SMTPAddressFailedException ssfe =
					(SMTPAddressFailedException)sfe;
			System.out.println("ADDRESS FAILED:");
			if (verbose)
			    System.out.println(ssfe.toString());
			System.out.println("  Address: " + ssfe.getAddress());
			System.out.println("  Command: " + ssfe.getCommand());
			System.out.println("  RetCode: " + ssfe.getReturnCode());
			System.out.println("  Response: " + ssfe.getMessage());
		    } else if (sfe instanceof SMTPAddressSucceededException) {
			System.out.println("ADDRESS SUCCEEDED:");
			SMTPAddressSucceededException ssfe =
					(SMTPAddressSucceededException)sfe;
			if (verbose)
			    System.out.println(ssfe.toString());
			System.out.println("  Address: " + ssfe.getAddress());
			System.out.println("  Command: " + ssfe.getCommand());
			System.out.println("  RetCode: " + ssfe.getReturnCode());
			System.out.println("  Response: " + ssfe.getMessage());
		    }
		}
	    } else {
		System.out.println("Got Exception: " + e);
		if (verbose)
		    e.printStackTrace();
	    }
	}
    }

    public static String collect(BufferedReader in) throws IOException {
	String line;
	StringBuffer sb = new StringBuffer();
	while ((line = in.readLine()) != null) {
	    sb.append(line);
	    sb.append("\n");
	}
	return sb.toString();
    }
}
