package org.maflt.ibidem

class Customer extends IbidemBaseDomain {
   
	String customer
    String urlKey
	
    static constraints = {
        customer(size: 1..45, blank: false)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        updatedBy(nullable: true)
        postedBy(nullable: true)
        ownerIdPerson(nullable: true)
        defaultMetacollection(nullable: true)
        urlKey(unique:true, blank:false, matches:"[a-z]+")
    }
    
    String toString() {
        return "${customer}" 
    }
}
