/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package muskca;

import java.io.File;

/**
 *
 * @author murloc
 */
public class FilePerso extends File
{
    public FilePerso(String pathname) 
    {
        super( new File(pathname).getAbsolutePath());
    }
    
}
