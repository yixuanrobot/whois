package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.TaggedRpslObject;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.api.whois.rdap.domain.ObjectFactory;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.Fn;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.Version;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class RdapObjectMapper {
    private TaggedRpslObject primaryTaggedRpslObject;
    private Queue<TaggedRpslObject> taggedRpslObjectQueue;
    private Object rdapResponse = new Object();

    public RdapObjectMapper(Queue<TaggedRpslObject> taggedRpslObjectQueue) {
        this.taggedRpslObjectQueue = taggedRpslObjectQueue;
    }

    public Object build() throws Exception {
        if (taggedRpslObjectQueue == null) {
            return rdapResponse;
        }

        if (!taggedRpslObjectQueue.isEmpty()) {
            primaryTaggedRpslObject = taggedRpslObjectQueue.poll();
        } else {
            throw new Exception("The rpsl queue is empty");
        }

        add(primaryTaggedRpslObject);

        /*while (!whoisObjectQueue.isEmpty()) {
            add(whoisObjectQueue.poll());
        }*/

        return rdapResponse;
    }

    private void add(TaggedRpslObject taggedRpslObject) {
        RpslObject rpslObject = taggedRpslObject.rpslObject;

        ObjectType rpslObjectType = rpslObject.getType();

        debug(rpslObject);

        if (rpslObjectType.getName().equals(ObjectType.PERSON.getName())) {

            Entity entity = new ObjectFactory().createEntity();
            entity.setHandle(rpslObject.getKey().toString());

            List<Object> vcardArray = (List<Object>) entity.getVcardArray();
            generateAndAddVCard(vcardArray, rpslObject);

            rdapResponse = entity;

        } else if (rpslObjectType.equals(ObjectType.ORGANISATION.getName())) {

        } else if (rpslObjectType.equals(ObjectType.ROLE.getName())) {

        } else if (rpslObjectType.equals(ObjectType.IRT.getName())) {

        } else if (rpslObjectType.equals(ObjectType.DOMAIN.getName())) {

            Domain domain = new ObjectFactory().createDomain();
            domain.setHandle(rpslObject.getKey().toString());

            rdapResponse = domain;
        } else if (rpslObjectType.equals(ObjectType.INETNUM.getName())
                || rpslObjectType.equals(ObjectType.INET6NUM.getName())) {

            Ip ip = new ObjectFactory().createIp();
            ip.setHandle(rpslObject.getKey().toString());

            rdapResponse = ip;
        }
    }

    private void debug(RpslObject rpslObject) {
        List<RpslAttribute> rpslAttributes = rpslObject.getAttributes();

        Iterator<RpslAttribute> iter = rpslAttributes.iterator();
        RpslAttribute ra;
        while (iter.hasNext()) {
            ra = iter.next();
            System.out.println(ra.getKey() + ":" + ra.getValue());
        }
    }

    private void generateAndAddVCard(List<Object> vcardArray, RpslObject rpslObject) {
        vcardArray.add("vcard");

        vcardArray.add(VcardObjectHelper.toObjects(new Version()));

        /*List<RpslAttribute> addressAttributes = rpslObject.findAttributes(AttributeType.ADDRESS);
        if (!addressAttributes.isEmpty()) {
            AddressType at = new AddressType();
            at.setExtendedAddress(attributeListToString(addressAttributes));
            //vCard.addAddress(at);
        }*/

        List<RpslAttribute> personAttributes = rpslObject.findAttributes(AttributeType.PERSON);
        if (!personAttributes.isEmpty()) {
            //vCard.setFormattedName(attributeListToString(personAttributes));
            Fn fn = new Fn();
            fn.setType("text");
            fn.setValue(attributeListToString(personAttributes));
            vcardArray.add(VcardObjectHelper.toObjects(fn));
        }

        /*List<RpslAttribute> phoneAttributes = rpslObject.findAttributes(AttributeType.PHONE);
        if (!phoneAttributes.isEmpty()) {
            TelephoneType tt = new TelephoneType(attributeListToString(phoneAttributes));
            //vCard.addTelephoneNumber(tt);
        }

        List<RpslAttribute> emailAttributes = rpslObject.findAttributes(AttributeType.E_MAIL);
        if (!emailAttributes.isEmpty()) {
            EmailType et = new EmailType(attributeListToString(emailAttributes));
            //vCard.addEmail(et);
        }*/
    }

    private String attributeListToString(List<RpslAttribute> rpslAttributes) {
        Iterator<RpslAttribute> rpslAttributeIterator = rpslAttributes.iterator();

        String output = "";

        while (rpslAttributeIterator.hasNext()) {
            output = output + " " + rpslAttributeIterator.next().getValue().trim();
        }

        return output.trim();
    }


}
