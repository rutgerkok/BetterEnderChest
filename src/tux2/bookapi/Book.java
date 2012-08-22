package tux2.bookapi;

/*
 * This class is a modified version of the Book API created by Joshua Reetz. 
 * You can find the orginal here:
 * http://forums.bukkit.org/threads/simple-temp-book-api.93562/
 * http://pastebin.com/NcMUtdEM
 */

/*
 * Copyright (C) 2012  Joshua Reetz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public class Book {

    private net.minecraft.server.ItemStack nmsItem = null;
    private CraftItemStack craftStack = null;

    public Book(org.bukkit.inventory.ItemStack item) {
        if(!Book.canContainText(item))
            throw new IllegalArgumentException(item.getType() + " cannot contain text!");
        
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            nmsItem = craftStack.getHandle();
        } else if (item instanceof org.bukkit.inventory.ItemStack) {
            craftStack = new CraftItemStack(item);
            nmsItem = craftStack.getHandle();
        }
    }
    
    // Added constructor
    public Book(Material material)
    {
        if(!Book.canContainText(material))
            throw new IllegalArgumentException(material + " cannot contain text!");
            
        craftStack = new CraftItemStack(new org.bukkit.inventory.ItemStack(material,1));
        nmsItem = craftStack.getHandle();
    }

    public String[] getPages() {
        NBTTagCompound tags = nmsItem.getTag();
        if (tags == null) {
            return null;
        }
        NBTTagList pages = tags.getList("pages");
        String[] pagestrings = new String[pages.size()];
        for (int i = 0; i < pages.size(); i++) {
            pagestrings[i] = pages.get(i).toString();
        }
        return pagestrings;
    }

    public String getAuthor() {
        NBTTagCompound tags = nmsItem.getTag();
        if (tags == null) {
            return null;
        }
        String author = tags.getString("author");
        return author;
    }

    public String getTitle() {
        NBTTagCompound tags = nmsItem.getTag();
        if (tags == null) {
            return null;
        }
        String title = tags.getString("title");
        return title;
    }

    public void setPages(String[] newpages) {
        NBTTagCompound tags = nmsItem.tag;
        if (tags == null) {
            tags = nmsItem.tag = new NBTTagCompound();
        }
        NBTTagList pages = new NBTTagList("pages");
        // we don't want to throw any errors if the book is blank!
        if (newpages.length == 0) {
            pages.add(new NBTTagString("1", ""));
        } else {
            for (int i = 0; i < newpages.length; i++) {
                pages.add(new NBTTagString("" + i + "", newpages[i]));
            }
        }
        tags.set("pages", pages);
    }

    public void addPages(String[] newpages) {
        NBTTagCompound tags = nmsItem.tag;
        if (tags == null) {
            tags = nmsItem.tag = new NBTTagCompound();
        }
        NBTTagList pages;
        if (getPages() == null) {
            pages = new NBTTagList("pages");
        } else {
            pages = tags.getList("pages");
        }
        // we don't want to throw any errors if the book is blank!
        if (newpages.length == 0 && pages.size() == 0) {
            pages.add(new NBTTagString("1", ""));
        } else {
            for (int i = 0; i < newpages.length; i++) {
                pages.add(new NBTTagString("" + pages.size() + "", newpages[i]));
            }
        }
        tags.set("pages", pages);
    }

    public void setAuthor(String author) {
        NBTTagCompound tags = nmsItem.tag;
        if (tags == null) {
            tags = nmsItem.tag = new NBTTagCompound();
        }
        if (author != null && !author.equals("")) {
            tags.setString("author", author);
        }
    }

    public void setTitle(String title) {
        NBTTagCompound tags = nmsItem.tag;
        if (tags == null) {
            tags = nmsItem.tag = new NBTTagCompound();
        }
        if (title != null && !title.equals("")) {
            tags.setString("title", title);
        }
    }

    public org.bukkit.inventory.ItemStack getItemStack() {
        return craftStack;
    }
    
    // Added several methods
    public static boolean canContainText(org.bukkit.inventory.ItemStack stack)
    {
        return Book.canContainText(stack.getType());
    }
    
    public static boolean canContainText(Material material)
    {
        if(material.equals(Material.WRITTEN_BOOK)) return true;
        if(material.equals(Material.BOOK_AND_QUILL)) return true;
        return false;
    }

}
